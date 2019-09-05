

import twitter4j.HashtagEntity;
import twitter4j.JSONArray;
import twitter4j.JSONObject;
import twitter4j.Query;
import twitter4j.Status;

import java.sql.Timestamp;

import geocodingGoogleAPI.Coordinates;

//@author John Neppel
//SE Practicum 2018-2019

/* This class is used to extract desired fields of data accordingly from Tweet metadata and encapsulate it
 * so it can be later be pulled and archived into MySQL. 
 */
public class TweetDataObject {
	private long TweetID; // the unique ID number associated with each Tweet
	private String TweetText; // the text of the Tweet itself
	private Coordinates coordinates;
	private Timestamp tweet_timestamp; // when the Tweet was shared YYYY-MM-DD HH:MM:SS
	private HashtagEntity[] arrayOfHashtags; // an array of hashtags that are located within each Tweet object

	private String queryTagText; // the text of the query

	private JSONObject JSONTweet; // stores the JSON-contents of the Tweet object

	private String hashtagsCSV; // the hashtags of the Tweet

	/*
	 * This constructor is for archiving Tweet/Status objects that CONTAIN
	 * geo-location metadata.
	 */
	public TweetDataObject(Status Tweet, Query q, JSONObject TweetAsJSON) {
		// pulls the appropriate data fields from the Tweet and Query Objects
		this.TweetID = Tweet.getId();
		this.TweetText = this.setTweetText(TweetAsJSON); // sets the text of the Tweet accordingly

		java.util.Date referenceDate = Tweet.getCreatedAt(); // creates a Java 'Date' object 
		this.tweet_timestamp = fromDateToTimestamp(referenceDate); // passes the Java 'Date' object into method which
																	// returns a sql.timestamp object

		this.arrayOfHashtags = Tweet.getHashtagEntities(); // an array of hashtags within the Tweet
		queryTagText = q.getQuery(); // gets the String of the tag portion of the query

		this.coordinates = setCoordinates(TweetAsJSON); // sets the coordinates for the Tweet object by extracting it
														// from the JSONObject

		this.JSONTweet = TweetAsJSON;
		this.hashtagsCSV = setHashtagsCSV();

	}

	/*
	 * This constructor is for archiving Tweet/Status objects that DO NOT CONTAIN
	 * geo-location metadata. Take note of the 'coords' parameter passsed into this
	 * constructor. The latitude/longitude encapsulated within this parameter have
	 * been generated outside of this class.
	 * 
	 */
	public TweetDataObject(Status Tweet, Query q, JSONObject TweetAsJSON, Coordinates coords) {
		// pulls the appropriate data fields from the Tweet and Query Objects
		this.TweetID = Tweet.getId();
		this.TweetText = this.setTweetText(TweetAsJSON);

		java.util.Date referenceDate = Tweet.getCreatedAt(); // creates a Java 'Date' object from the method within the
																// Status class
		this.tweet_timestamp = fromDateToTimestamp(referenceDate); // passes the Java 'Date' object into method which
																	// returns a sql.timestamp object

		this.arrayOfHashtags = Tweet.getHashtagEntities(); // an array of hashtags within the Tweet
		queryTagText = q.getQuery(); // gets the String of the tag portion of the query

		this.coordinates = coords; // sets the coordinates via the parameter passed into constructor

		this.JSONTweet = TweetAsJSON;
		this.hashtagsCSV = setHashtagsCSV();

	}

	public String setHashtagsCSV() {
		String listOfHashtags = "";

		if (JSONTweet.has("retweetedStatus")) {
			// The set of hashtags for Tweets that are retweets do NOT always contain
			// all of the hashtags that it should. Therefore, the full list of hashtags
			// needs to be extracted from the embedded retweeted status JSONObject
			try {
				JSONObject retweetedStatusObject = this.JSONTweet.getJSONObject("retweetedStatus");
				JSONArray hashtagEntities = retweetedStatusObject.getJSONArray("hashtagEntities");

				for (int i = 0; i < hashtagEntities.length(); i++) {
					JSONObject hashtagIndex = hashtagEntities.getJSONObject(i);
					String hashtagText = hashtagIndex.getString("text").toLowerCase();
					if (i < hashtagEntities.length() - 1) { // as long as the loop is NOT on the last index of the array
						listOfHashtags = listOfHashtags + hashtagText + ",";
					} else { // the last hashtag in the CSV list DOES NOT get a comma
						listOfHashtags = listOfHashtags + hashtagText;
					}

				} // end of FOR loop
			} // end of try
			catch (Exception e) {
				System.out.println(e);
				return "";
			}
		}

		// For Tweets that ARE NOT retweets, the FULL list of hashtags can successfully
		// be extracted and retrieved from the Tweet using the following method (which
		// uses the Twitter4J library functions)
		else {
			for (int currIndex = 0; currIndex < arrayOfHashtags.length; currIndex++) {
				if (currIndex < arrayOfHashtags.length - 1) { // as long as the current Index is NOT the last index of// the array
					// Append to the string with the hashtag-text and a comma
					listOfHashtags = listOfHashtags + arrayOfHashtags[currIndex].getText().toLowerCase() + ",";
				} else {
					// the last value from the array doesn't have a comma after it.
					listOfHashtags = listOfHashtags + arrayOfHashtags[currIndex].getText().toLowerCase(); 
				}
			}
		}

		return listOfHashtags;

	}

	/*
	 * Method takes in a java.util.Date object as a parameter and creates a
	 * representative sql.timestamp object based on the data within the parameter.
	 * This is done so the timestamp of the Tweet can be effectively stored in the
	 * MySQL Database Table.
	 */
	public Timestamp fromDateToTimestamp(java.util.Date dateObject) {
		// gets the number of milliseconds since January 1, 1970, 00:00:00 GMT
		// represented by this Date object.
		long milliseconds = dateObject.getTime();
		Timestamp ts = new Timestamp(milliseconds); // passes in the variable that corresponds to the value passed into
													// Timestamp constructor
		return ts;
	}

	/*
	 * Method takes in a String parameter, checks to see if the first character of
	 * it contains a '#' and returns the string without the beginning '#' (if it
	 * contains it.) If the parameter doesn't contain a '#' as the first character,
	 * the string is simply returned unmodified. This method is necessary because
	 * the twitter4j.HashtagEntity object will return the text of the hashtag within
	 * the Tweet without the '#'. That means in order to compare the hashtagEntities
	 * within a Tweet to a query searching for a particular hashtag (e.g. #Metoo)
	 * the '#' needs to be removed.
	 * 
	 */
	public String removeHashtagFromString(String hashtagKeyword) {
		String modifiedString = "";
		if (hashtagKeyword.charAt(0) == '#') {
			modifiedString = hashtagKeyword.substring(1, hashtagKeyword.length()); // creates a substring without '#'
			modifiedString = modifiedString.toLowerCase();
			return modifiedString; // returns the String without the '#'
		} else {
			return hashtagKeyword;
		}
	}

	private Coordinates setCoordinates(JSONObject TweetAsJSON) {
		double lat = 0;
		double longit = 0;
		int numOfGeoPoints = 0; // keeps track of the number of Geo-coordinate pairs within the JSON-Tweet
								// object (should always be 4)
		JSONObject placeData = new JSONObject(); // will store 'place' data of the Tweet (if necessary)
		Coordinates coords = new Coordinates();

		if (TweetAsJSON.has("geoLocation")) { // SOME Tweets contain the 'geoLocation' key, which is an exact location
												// data point
			JSONObject geoLocationPoint = TweetAsJSON.getJSONObject("geoLocation"); // extracts the embedded geoLocation
																					// JSONObject

			// System.out.println(geoLocationPoint.toString()); //DEBUGGING
			lat = geoLocationPoint.getDouble("latitude");
			longit = geoLocationPoint.getDouble("longitude");
			coords.setLatitude(lat);
			coords.setLongitude(longit);
		}

		else {
			try {
				placeData = TweetAsJSON.getJSONObject("place"); // attempts to get the 'place' JSONObject which contains
																// geo-data.
			} catch (Exception e) {
				System.out.println(e);
				return coords; 
			}

			// ALL Tweets with geo-coordinate metadata have a bounding-box, which is a set
			// of geo-coordinate pairs surrounding the location
			JSONArray boundingBoxCoord = placeData.getJSONArray("boundingBoxCoordinates");
			for (int i = 0; i < boundingBoxCoord.length(); i++) {
				JSONArray coordArray = boundingBoxCoord.getJSONArray(i); // gets the embedded array object
				for (int j = 0; j < coordArray.length(); j++) {
					JSONObject coordElement = coordArray.getJSONObject(j);// gets the JSONObject at the current index
					lat = lat + coordElement.getDouble("latitude");
					longit = longit + coordElement.getDouble("longitude");
					numOfGeoPoints++;
				}
			}
			lat = lat / numOfGeoPoints; // takes the average of all the latitude points
			longit = longit / numOfGeoPoints; // takes the average of all the longitude points
			coords.setLatitude(lat);
			coords.setLongitude(longit);

		} // end of 'else'

		return coords;
	}

	/*
	 * Method extracts the text of the Tweet and returns it. If the Tweet is a
	 * retweet of another Status, the method will ensure that the returned text of
	 * the Tweet will contain "RT" and the @username, along with the full text of
	 * the Tweet.
	 */
	public String setTweetText(JSONObject TweetAsJSON) {
		String tweetText = "";

		// Checks to see if the Tweet is a 'retweet' of another Tweet. Tweets that are
		// retweeted are automatically truncated by the Twitter Search API. In order to
		// get the full non-truncated Tweet, we need to extract the embedded 'retweetedStatus'
		// JSONObject. The following checks to ensure that the Tweet is a Retweet
		if (TweetAsJSON.getString("text").substring(0, 3).equals("RT ") && TweetAsJSON.has("retweetedStatus")) {
			JSONObject retweetedStatus = TweetAsJSON.getJSONObject("retweetedStatus"); // the Tweet that was retweeted
																						// by the user
			String originalStatus = TweetAsJSON.getString("text");

			for (int i = 0; i < originalStatus.length(); i++) { // This loop produces a substring encompassing the "RT" and
																// the @username: whose Tweet was retweeted
				tweetText = tweetText + originalStatus.charAt(i) + "";
				if (originalStatus.charAt(i) == ':') {
					break; // a colon represents the end of the @username
				}
			}

			tweetText = tweetText + " " + retweetedStatus.getString("text"); // Appends the full text of the retweet
		}

		else { // if the Tweet is not a Retweet
			tweetText = TweetAsJSON.getString("text"); // simply get the text of the Tweet
		}
		return tweetText;
	}

	public long getTweetID() {
		return this.TweetID;
	}

	public String getHashtagsCSV() {
		return this.hashtagsCSV;
	}

	public String getTweetText() {
		return this.TweetText;
	}

	public Coordinates getCoordinates() {
		return this.coordinates;
	}

	public Timestamp getTweetTimestamp() {
		return this.tweet_timestamp;
	}

	public String getQueryTagText() {
		return this.queryTagText;
	}

	public HashtagEntity[] getHashtageEntity() {
		return this.arrayOfHashtags;
	}

}
