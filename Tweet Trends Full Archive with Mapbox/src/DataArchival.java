import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import geocodingGoogleAPI.Coordinates;
import twitter4j.JSONObject;
import twitter4j.TwitterException;
//@author John Neppel
//SE Practicum 2018-2019

/* This class is responsible for providing and managing the connections(s)
 * to the MySQL Database and executing "select" queries to 
 * determine whether certain fields of data (e.g Twitter User bio-locations)
 * are already located within the tables and retrieve those values as well.
 * 
 */
public class DataArchival {
	private String url;
	private String username;
	private String password;;
	private static Connection connection;

	// private KeepAliveThread keepAlive;

	private static DataArchival instance;

	/* Constructor */
	private DataArchival() {
		configureSettingsToDatabase(); // initializes the connection settings via the .ini file
		setUpConnectionToDatabase(); // sets up the connection using configured settings

		// keepAlive.start();

	}

	/*
	 * Followed the singleton pattern to ensure that only one set of connections to
	 * the database are opened/closed. This reduces the likelyhood of a
	 * connection-related resource leak
	 */
	public static DataArchival getInstance() {
		if (instance == null) {
			instance = new DataArchival();
			return instance;
		} else {
			return instance;
		}
	}

	/*
	 * Method reads in the configuration.ini file to get the connectivity settings
	 * for the MySQL tweet_trends Database schema
	 */
	private void configureSettingsToDatabase() {
		try (FileReader fileReader = new FileReader("configuration.ini")) {
			// Gets the data parameters necessary for connecting from the database
			Properties properties = new Properties();
			properties.load(fileReader);
			url = properties.getProperty("url");
			username = properties.getProperty("username");
			password = properties.getProperty("password");

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	// // Returns the configured database connection object
	public Connection getConnectionToDatabase() {
		//Checks if database connection is inactive. In that case,
	   // a new one will be created. This helps solve the issue of
	   //MySQL unexpectedly closing the connection.
		if(isDatabaseConnectionActive()==false) {
			setUpConnectionToDatabase();
		}
		
		return connection;
	
}

	/*
	 * Uses the url, username and password instance variables to set up connection
	 * to the DB
	 */
	private void setUpConnectionToDatabase() {
		try {
			connection = DriverManager.getConnection(url, username, password); // connects to server using url,
																				// username, password.
			connection.setAutoCommit(false);

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/*
	 * Method queries the database for the largest TweetID associated with the text
	 * of a query tag (e.g. #Metoo, #Hello, or any other query). It returns the
	 * result of the query, which may be the largest Tweet ID number in response to
	 * the executed query, or NULL if nothing is found. This method is to help avoid
	 * archiving duplicated Tweets associated with a particular query.
	 * 
	 * Tweet ID's are sequential, meaning the most recently shared ones are larger
	 * than the older ones.
	 */
	public Object getLargestAssocTweetID(String queryTagText) {
		String statement = "SELECT max(TweetID) FROM tweet_data WHERE(query_tag_id= (SELECT Query_Tag_ID FROM tweet_tags WHERE QueryTagText=?))";
		Object queryResult = null;
		PreparedStatement query = null;
		ResultSet resultSet = null;

		try {
			query = getConnectionToDatabase().prepareStatement(statement);
			query.setString(1, queryTagText); // adds the parameter to the query-string
			resultSet = query.executeQuery();

			if (resultSet.next()) { // if there is anything within the result set
				queryResult = resultSet.getObject(1);
			}

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			try {
				if (query != null) {
					query.close();
				}
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return queryResult;
	}

	/*
	 * Method queries the database for the smallest TweetID associated with the text
	 * of a query tag (e.g. #Metoo, #Hello, or any other query). It returns the
	 * result of the query, which may be the smallest Tweet ID number in response to
	 * the executed query, or NULL if nothing is found. This method is to help avoid
	 * archiving duplicated Tweets associated with a particular query.
	 * 
	 * Tweet ID's are sequential, meaning the most recently shared ones have larger
	 * ID's than the older ones.
	 */
	public Object getSmallestAssocTweetID(String queryTagText) {
		String statement = "SELECT min(TweetID) FROM tweet_data WHERE(query_tag_id= (SELECT Query_Tag_ID FROM tweet_tags WHERE QueryTagText=?))";
		Object queryResult = null;
		PreparedStatement query = null;
		ResultSet resultSet = null;

		try {
			query = getConnectionToDatabase().prepareStatement(statement);
			query.setString(1, queryTagText); // adds the parameter to the query-string
			resultSet = query.executeQuery();

			if (resultSet.next()) { // if there is anything within the result set
				queryResult = resultSet.getObject(1); // the executed query will only contain
			}

		} catch (Exception e) {
			System.out.println(e);
		}

		finally {
			try {
				if (query != null) {
					query.close();
				}
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}

		return queryResult;
	}

	/*
	 * Method checks whether connection to the database is active and successful.
	 * Returns true if it is actively connected to the database and false otherwise.
	 */
	public boolean isDatabaseConnectionActive() {
		try {
			if (connection.equals(null) || connection.isClosed() || connection.isValid(12)==false) {
				return false;
			} else {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isLocationDataArchived(String bioLocation) {
		String queryStatement = "SELECT EXISTS(SELECT* FROM archived_location_data WHERE LocationName=?);";
		PreparedStatement query = null;
		ResultSet result = null;
		boolean isArchived = false;
		try {
			query = getConnectionToDatabase().prepareStatement(queryStatement);
			query.setString(1, bioLocation);
			result = query.executeQuery();

			if (result.next()) { // there will always be a result in response to query; either 1 or 0 for true or
									// false
				if (result.getBoolean(1) == true) {
					isArchived = true;
				} else {
					isArchived = false;
				}

			}

		} catch (Exception e) {
			System.out.println(e);
		}

		finally {
			try {
				if (query != null) {
					query.close();
				}
				if (result != null) {
					result.close(); // HAD AN ISSUE HERE
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return isArchived;

	}

	public Coordinates getCoordsFromArchivedLocationData(String bioLocation) {
		Coordinates coords = new Coordinates();
		double lat = 0;
		double longit = 0;
		String queryStatement = "SELECT latitude, longitude FROM archived_location_data WHERE LocationName=?;";
		PreparedStatement query = null;
		ResultSet result = null;

		try {
			query = getConnectionToDatabase().prepareStatement(queryStatement);
			query.setString(1, bioLocation);
			result = query.executeQuery();
			if (result.next()) {
				lat = result.getDouble(1);
				longit = result.getDouble(2);
			}
			coords.setLatitude(lat);
			coords.setLongitude(longit);
		} catch (Exception e) {
			System.out.println(e);
			return coords;
		} finally {
			try {
				if (query != null) {
					query.close();
				}
				if (result != null) {
					result.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return coords;

	}

	/*
	 * Method takes in a String representation of a location name and a Coordinates
	 * object. It will first check the database to see if a row already exists with
	 * that location name because it is a unique index. Then it will insert the
	 * location name and the latitude and longitude associated with the Coordinates
	 * object into the database.
	 * 
	 */
	public void archiveLocationData(String bioLocation, Coordinates generatedCoords) {

		String queryStatement = "INSERT IGNORE INTO archived_location_data(LocationName, latitude, longitude) VALUES(?,?,?);";
		PreparedStatement query = null;

		try {
			query = getConnectionToDatabase().prepareStatement(queryStatement);
			query.setString(1, bioLocation);
			query.setDouble(2, generatedCoords.getLatitude());
			query.setDouble(3, generatedCoords.getLongitude());
			query.executeUpdate();

		} catch (Exception e) {
			System.out.println(e);
		}

		finally {
			try {
				if (query != null) {
					query.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	public void closeDatabaseConnection() {
		try {
			if (connection.equals(null)==false) {
				connection.commit(); // commits any uncommitted transactions left
				connection.close();
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void archiveTweetMetadata(TweetDataObject tweet) {
		try {
			this.generateTweetTagsQuery(tweet);
			this.generateTweetDataQuery(tweet);
			getConnectionToDatabase().commit();
			this.generateAssocTagsDescipQueries(tweet);
			this.generateAssocTagsQueries(tweet);
		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}

	/*
	 * Method uses certain instance variables from this class to generate an insert
	 * query for the tweet_tags table. It will then execute the generated query.
	 */
	private void generateTweetTagsQuery(TweetDataObject tweet) {
		PreparedStatement existsStatement = null;
		PreparedStatement statement1 = null;
		ResultSet existsSet = null;
		try {
			String existsQuery = "SELECT EXISTS(SELECT* FROM tweet_tags WHERE QueryTagText=?);"; // will test whether
																									// the query
																									// tag/text has been
																									// archived
																									// previously
			String insert1 = "INSERT INTO tweet_tags(QueryTagText) VALUES(?);";

			existsStatement = getConnectionToDatabase().prepareStatement(existsQuery);
			existsStatement.setString(1, tweet.getQueryTagText());
			existsSet = existsStatement.executeQuery();

			if (existsSet.next()) { // this resultSet will always consist of 1 or 0 representing true or false

				if (existsSet.getBoolean(1) == false) { // false means that the text of this query hasn't been archived
														// within this table yet
					statement1 = getConnectionToDatabase().prepareStatement(insert1);
					statement1.setString(1, tweet.getQueryTagText());
					statement1.executeUpdate();
				}

			}

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			try {
				if (statement1 != null) {
					statement1.close();
				}

				if (existsStatement != null) {
					existsStatement.close();
				}
				if (existsSet != null) {
					existsSet.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	/*
	 * Method uses all of the metadata pulled from the Tweet and coordinate objects
	 * to generate an insert query for archiving data to the tweet_data table. It
	 * will then execute the generated query.
	 */
	private void generateTweetDataQuery(TweetDataObject tweet) {
		String insert2 = "INSERT INTO tweet_data(TweetID, TweetText, query_tag_id, latitude, longitude, tweet_timestamp, Hashtags)"
				+ " VALUES(?,?,(SELECT Query_Tag_ID FROM tweet_tags WHERE QueryTagText= ?),?,?,?,?);";
		PreparedStatement statement2 = null;

		try {
			statement2 = getConnectionToDatabase().prepareStatement(insert2);
			statement2.setLong(1, tweet.getTweetID());
			statement2.setString(2, tweet.getTweetText());
			statement2.setString(3, tweet.getQueryTagText());
			statement2.setDouble(4, tweet.getCoordinates().getLatitude());
			statement2.setDouble(5, tweet.getCoordinates().getLongitude());
			statement2.setTimestamp(6, tweet.getTweetTimestamp());
			statement2.setString(7, tweet.getHashtagsCSV());

			statement2.executeUpdate(); // executes the query statement
			if (statement2 != null) {
				statement2.close();
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	/*
	 * Method generates insert queries for the associated_tags_description table
	 * using the text from the query and the array of hashtags associated with the
	 * Tweet. It will then generate the executed queries.
	 */
	private void generateAssocTagsDescipQueries(TweetDataObject tweet) {
		String queryTextWithoutHashtag = tweet.removeHashtagFromString(tweet.getQueryTagText()); // this stores the text
																									// of the query
																									// which will be
																									// used for
																									// comparison
		String insert3 = "INSERT IGNORE INTO associated_tags_description(tag_text) VALUES(?);";
		String existsQuery = "SELECT EXISTS(SELECT* FROM associated_tags_description WHERE tag_text=?);";
		ArrayList<String> HashtagsArray = tweet.getHashtagsArray();

		for (int currIndex = 0; currIndex < HashtagsArray.size(); currIndex++) {
			try {
				PreparedStatement statement = null;
				PreparedStatement existsStatement = null;

				String currHashtagText = HashtagsArray.get(currIndex).toLowerCase(); // gets the Hashtag text at the
																						// current index of the array
				if (queryTextWithoutHashtag.equalsIgnoreCase(currHashtagText)) {// if the text of the query matches the
																				// text of the hashtag at the current
																				// index
					continue; // returns the execution back to the 'for' loop, we do not want to archive an
								// associated hashtag that corresponds to the hashtag within the query
				}

				existsStatement = getConnectionToDatabase().prepareStatement(existsQuery);
				existsStatement.setString(1, currHashtagText); // will check to see whether the text of this hashtag has
																// been previously been archived into this table (it's
																// an unique index)
				ResultSet existsSet = existsStatement.executeQuery();

				if (existsSet.next()) { // START OUTER IF
					if (existsSet.getBoolean(1) == true) { // true indicates that it has been previously been archived
						// the following closes opened the DB resources
						if (existsStatement != null) {
							existsStatement.close();
						}
						if (existsSet != null) {
							existsSet.close();
						}

					} else { // this is if the resultset has false within it, which indicates that the
								// hashtag string NEEDS to be archived
						statement = getConnectionToDatabase().prepareStatement(insert3);
						statement.setString(1, currHashtagText); // sets the parameter as the Hashtag text at the
																	// current index of the array
						statement.executeUpdate(); // executes the query statement to insert it

						if (statement != null) {
							statement.close();
						}
					}
				} // END OUTER IF

			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	/*
	 * Method generates insert queries for the tweet_tags table using the text from
	 * the query, the ID from the Tweet, and the array of hashtags associated with
	 * the Tweet. It will then execute the generated queries.
	 */
	private void generateAssocTagsQueries(TweetDataObject tweet) {
		String queryTextWithoutHashtag = tweet.removeHashtagFromString(tweet.getQueryTagText()); // this stores the text
																									// of the query
																									// (without any '#')
																									// which will be
																									// used for
																									// comparison
		String queryString = "INSERT INTO associated_tags(associated_tag_id, TweetID) "
				+ "VALUES((SELECT ID FROM associated_tags_description WHERE tag_text=?), ?);";
		ArrayList<String> HashtagsArray = tweet.getHashtagsArray();

		for (int currIndex = 0; currIndex < HashtagsArray.size(); currIndex++) {
			try {
				PreparedStatement statement = null;
				String currHashtagText = HashtagsArray.get(currIndex); // gets the Hashtag text at the current index of
																		// the array
				if (queryTextWithoutHashtag.equalsIgnoreCase(currHashtagText)) {
					continue; // returns the execution to the 'for' loop; hashtags that are identical to the
								// query tag are NOT archived
				}
				statement = getConnectionToDatabase().prepareStatement(queryString);
				statement.setString(1, currHashtagText);
				statement.setLong(2, tweet.getTweetID()); // sets the parameter to the TweetID number
				statement.executeUpdate(); // executes the query statement
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}

	}

	public static void main(String args[]) throws TwitterException, SQLException {
		DataArchival archiver = DataArchival.getInstance();
		Coordinates coords = archiver.getCoordsFromArchivedLocationData("West Long Branch");
		System.out.println(coords);
		System.out.println(archiver.isLocationDataArchived("FRUITLDJFLK"));

	}

}

// https://coderanch.com/t/300886/databases/Proper-close-Connection-Statement-ResultSet

// https://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html
