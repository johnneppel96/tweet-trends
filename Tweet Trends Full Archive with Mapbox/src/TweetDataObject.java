import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import twitter4j.HashtagEntity;
import twitter4j.JSONArray;
import twitter4j.JSONObject;
import twitter4j.Query;
import twitter4j.Status;

import geocodingGoogleAPI.Coordinates;

//@author John Neppel
//SE Practicum 2018-2019
public class TweetDataObject extends DataObject {
	private long TweetID; // the unique ID number associated with each Tweet
	private String TweetText = ""; // the text of the Tweet itself
	private Coordinates coordinates;
	private Timestamp tweet_timestamp; // when the Tweet was shared YYYY-MM-DD HH:MM:SS
	private ArrayList<String> HashtagsArray; // an array of hashtags that are located within each Tweet object
	private String hashtagsCSV;

	private String queryTagText; // the text of the query

	/*
	 * This constructor is for archiving Tweet/Status objects that CONTAIN
	 * geo-location metadata.
	 */
	public TweetDataObject(QueryObject q, JSONObject TweetAsJSON) {
		// pulls the appropriate data fields from the Tweet and Query Objects
		this.TweetID = this.setTweetID(TweetAsJSON);
		this.TweetText = this.setTweetText(TweetAsJSON); // sets the text of the Tweet accordingly

		java.util.Date referenceDate = setTweetCreationTimestamp(TweetAsJSON);
		this.tweet_timestamp = fromDateToTimestamp(referenceDate); // passes the Java 'Date' object into method which
																	// returns a sql.timestamp object

		this.HashtagsArray = this.setArrayOfHashtags(TweetAsJSON);
		this.hashtagsCSV = setHashtagsCSVString();
		queryTagText = "#" + q.getQueryStatement(); // gets the String of the tag portion of the query

		this.coordinates = new Coordinates();
		this.coordinates = setCoordinates(TweetAsJSON); // sets the coordinates for the Tweet object by extracting it
														// from the JSONObject

	}

	/*
	 * This constructor is for archiving Tweet/Status objects that DO NOT CONTAIN
	 * geo-location metadata. Take note of the 'coords' parameter passsed into this
	 * constructor. The latitude/longitude encapsulated within this parameter have
	 * been generated outside of this class via the Mapbox Forward Geocoding API.
	 * 
	 */
	public TweetDataObject(QueryObject q, JSONObject TweetAsJSON, Coordinates coords) {
		// pulls the appropriate data fields from the Tweet and Query Objects
		this.TweetID = this.setTweetID(TweetAsJSON);
		this.TweetText = this.setTweetText(TweetAsJSON);// sets the text of the Tweet accordingly

		java.util.Date referenceDate = setTweetCreationTimestamp(TweetAsJSON);
		this.tweet_timestamp = fromDateToTimestamp(referenceDate); // passes the Java 'Date' object into method which
																	// returns a sql.timestamp object

		this.HashtagsArray = this.setArrayOfHashtags(TweetAsJSON); // an array of hashtags within the Tweet
		this.hashtagsCSV = setHashtagsCSVString();
		queryTagText = "#" + q.getQueryStatement(); // gets the String of the tag portion of the query

		this.coordinates = coords; // sets the coordinates via the parameter passed into constructor
	}

	private long setTweetID(JSONObject jsonTweet) {
		long id = jsonTweet.getLong("id");
		return id;
	}

	/*
	 * Will extract the creation date metadata of the Tweet from the JSON and create
	 * a representative java.util.Date object. Dates through the premium endpoint
	 * are in the following format: Fri Mar 08 06:32:25 +0000 2019
	 */
	private java.util.Date setTweetCreationTimestamp(JSONObject jsonTweet) {
		String createdAtDate = jsonTweet.getString("created_at");
		String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy"; // This is how Twitter expresses dates
		Date parsedDate = null;

		SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
		sf.setLenient(true);

		try {
			parsedDate = sf.parse(createdAtDate);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}

		return parsedDate;
	}

	private ArrayList<String> setArrayOfHashtags(JSONObject jsonTweet) {
		ArrayList<String> extractedHashtags = new ArrayList<String>();

		// Tweets that are retweets do not always contain all of the hashtags within the
		// the outer hashtags array fields
		// hashtags array object; therefore they must be extracted from the embedded
		// retweeted_status object
		if (jsonTweet.getString("text").substring(0, 3).equals("RT ") && jsonTweet.has("retweeted_status")) {
			JSONObject retweetedStatus = jsonTweet.getJSONObject("retweeted_status");
			JSONObject entities = retweetedStatus.getJSONObject("entities");
			JSONArray hashtags = entities.getJSONArray("hashtags");
			for (int i = 0; i < hashtags.length(); i++) {
				JSONObject objectAtIndex = hashtags.getJSONObject(i);
				String hashtagText = objectAtIndex.getString("text"); // gets the text of the Tweet embedded within the
																		// JSONObject
				extractedHashtags.add(hashtagText);
			} // end for loop
			return extractedHashtags;
		}

		// The following is for Tweets that are NOT retweets, therefore the list of
		// hashtags
		// can ordinarily be extracted from the base Tweet JSON Object.
		// With some Tweets, the list of hashtags are embedded within the "entities"
		// JSON-Object
		if (jsonTweet.has("entities")) {
			JSONObject entities = jsonTweet.getJSONObject("entities");
			JSONArray hashtags = entities.getJSONArray("hashtags");
			for (int i = 0; i < hashtags.length(); i++) {
				JSONObject objectAtIndex = hashtags.getJSONObject(i);
				String hashtagText = objectAtIndex.getString("text"); // gets the text of the Tweet embedded within the JSONObject
				extractedHashtags.add(hashtagText);
			}
		}

		else { // other tweets directly have a "hashtagEntities" array which isn't embedded
			JSONArray hashtagArray = jsonTweet.getJSONArray("hashtagEntities");
			for (int i = 0; i < hashtagArray.length(); i++) {
				JSONObject arrayElement = hashtagArray.getJSONObject(i);
				extractedHashtags.add(arrayElement.getString("text")); // extracts the text value of the contained
																		// hashtag
			}
		}

		return extractedHashtags;
	}

	/*
	 * Generates a comma-separated-value string based on the hashtags of the
	 * Tweet/Status object.
	 * 
	 */
	public String setHashtagsCSVString() {
		String HashtagsCSV = "";
		for (int currIndex = 0; currIndex < HashtagsArray.size(); currIndex++) {
			if (currIndex < HashtagsArray.size() - 1) { // as long as the current Index is NOT the last index of the
														// array
				HashtagsCSV = HashtagsCSV + HashtagsArray.get(currIndex).toLowerCase() + ","; // Append to the string
																								// with the hashtag-text
																								// and a comma
			} else {
				HashtagsCSV = HashtagsCSV + HashtagsArray.get(currIndex).toLowerCase(); // the last value from the array
																						// doesn't have a comma after
																						// it.
			}
		}
		return HashtagsCSV;
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

	/*
	 * Returns a set of generated coordinates via embedded coordinate data located
	 * within the Tweet metadata.
	 * 
	 */
	@SuppressWarnings("unused")
	private Coordinates setCoordinates(JSONObject TweetAsJSON) {
		double lat = 0;
		double longit = 0;
		int numOfGeoPoints = 0; // keeps track of the number of Geo-coordinate pairs within the JSON-Tweet
								// object (should always be 4)
		JSONObject placeData = new JSONObject(); // will store 'place' data of the Tweet (if necessary)
		Coordinates coords = new Coordinates();

		try {
			placeData = TweetAsJSON.getJSONObject("place"); // attempts to get the 'place' JSONObject which contains
															// geo-data.
		} catch (Exception e) {
			System.out.println(e);
			return coords; // if an error is thrown, end further execution of the method
		}

		JSONObject boundingBoxCoord = placeData.getJSONObject("bounding_box"); // extracts the boundingBox which
																				// contains sets of coordinates

		// The coordinates are an "Array of Array of Array of Float". The following extracts it.
		JSONArray coordinatesArray = boundingBoxCoord.getJSONArray("coordinates");

		JSONArray coordArrayElement = coordinatesArray.getJSONArray(0); // gets the embedded array object, will always
																		// be at index 0
		for (int i = 0; i < coordArrayElement.length(); i++) {
			JSONArray latLongElement = coordArrayElement.getJSONArray(i);
			// Within the embedded array, the first element is longitude, second element is
			// the latitude
			// Adds up all of the consecutive lat and longit points for bounding-box to get
			// the middle
			longit = longit + latLongElement.getDouble(0);
			lat = lat + latLongElement.getDouble(1);
			numOfGeoPoints++;
		}
		lat = lat / numOfGeoPoints; // takes the average of all the latitude points
		longit = longit / numOfGeoPoints; // takes the average of all the longitude points
		coords.setLatitude(lat);
		coords.setLongitude(longit);

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
		// get the
		// full non-truncated Tweet, we need to extract the embedded 'retweetedStatus'
		// JSONObject.

		if (TweetAsJSON.getString("text").substring(0, 3).equals("RT ") && TweetAsJSON.has("retweeted_status")) { 
			JSONObject retweetedStatus = TweetAsJSON.getJSONObject("retweeted_status"); // the Tweet that was retweeted
																						// by the user
			String originalStatus = TweetAsJSON.getString("text");

			for (int i = 0; i < originalStatus.length(); i++) { // This produces a substring encompassing the "RT" and
																// the @username: whose Tweet was retweeted
				tweetText = tweetText + originalStatus.charAt(i) + "";
				if (originalStatus.charAt(i) == ':') {
					break; // a colon represents the end of the @username:
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

	public String getTweetText() {
		return this.TweetText;
	}

	public Coordinates getCoordinates() {
		return this.coordinates;
	}

	public Timestamp getTweetTimestamp() {
		return this.tweet_timestamp;
	}

	public ArrayList<String> getHashtagsArray() {
		return this.HashtagsArray;
	}

	public String getQueryTagText() {
		return this.queryTagText;
	}

	public String getHashtagsCSV() {
		return this.hashtagsCSV;
	}
}
