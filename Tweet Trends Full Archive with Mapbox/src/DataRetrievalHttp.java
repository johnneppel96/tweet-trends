import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.awt.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import twitter4j.JSONArray;
import twitter4j.JSONObject;
import twitter4j.RateLimitStatus;
import twitter4j.RateLimitStatusEvent;
import twitter4j.Status;

import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.sql.SQLException;
import java.util.Properties;

import geocodingGoogleAPI.Coordinates;
import geocodingGoogleAPI.Geocoder;
import mapboxapiconnector.ForwardGeocoderMapbox;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

//@author John Neppel
//SE Practicum 2018-2019
public class DataRetrievalHttp {

	private final int MAX_NUM_OF_DATA_POINTS; // maximum number of data points per batch for the version of the API
	private ForwardGeocoderMapbox MapboxAPI;

	private String consumerKey; // CONSUMER KEY
	private String consumerSecret; // CONSUMER SECRET
	private String accessToken; // TOKEN
	private String accessTokenSecret; // SECRET TOKEN
	private final String restAPIURL = "https://api.twitter.com/1.1/tweets/search/fullarchive/TweetGeoCodesDev.json";
	private DataArchival archiver;

	// https://api.twitter.com/1.1/tweets/search/fullarchive/TweetGeoCodesDev.json?query=MeToo&toDate=201903060000

	public DataRetrievalHttp() {
		this.configConnectionToAPI();
		MAX_NUM_OF_DATA_POINTS = getMaxNumOfDataPointsPerRequest();
		MapboxAPI = ForwardGeocoderMapbox.getInstance();
		archiver = DataArchival.getInstance();
	}

	/*
	 * Method prints out the Http-get Response Entity for the query parameter.
	 * 
	 */
	public void printHttpResponse(QueryObject query) {
		try {
			String HttpResponse = this.getAPIResponseEntityString(query);
			System.out.println(HttpResponse);

		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	/*
	 * Returns a String-representation of a http-get response entity from the
	 * Twitter API in response to the query passed into the method.
	 */

	public String getAPIResponseEntityString(QueryObject query) {
		// Appends the query accordingly to the Base-API URL. This URL which
		// will be executed via an http-get request must be formatted this way
		String urlWithQuery = this.restAPIURL + "?query=" + query.getQueryStatement() + "&fromDate="
				+ query.getFromDate() + "&toDate=" + query.getToDate();

		// Appends the Next token to the url query (if it has one)
		if (query.hasNextToken()) {
			urlWithQuery = urlWithQuery + "&next=" + query.getNextToken();
		}
		// System.out.println(urlWithQuery);
		try {
			// sets the oauth. parameters which will be incorporated into the http get
			// requests
			OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
			oAuthConsumer.setTokenWithSecret(accessToken, accessTokenSecret);

			HttpGet httpGet = new HttpGet(urlWithQuery);
			oAuthConsumer.sign(httpGet); // adds oauth parameters to the http-object

			HttpClientBuilder builder = HttpClientBuilder.create()
					.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build());

			CloseableHttpClient httpClient = builder.build();
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity entity = httpResponse.getEntity();

			String responseString = EntityUtils.toString(entity, "UTF-8");
			httpClient.close();
			return responseString;
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	/*
	 * Takes the API"s response to a query in the form of a JSON-formatted String
	 * and converts it to a JSONObject so it can be easily utilized for data
	 * analysis.
	 */
	public JSONObject getAPIResponseJSONOb(QueryObject query) {
		String APIResponseString = this.getAPIResponseEntityString(query); // gets the API's response to query as a
																			// JSON-formattedString
		JSONObject responseJSON = new JSONObject(APIResponseString); // creates a JSONObject from the API's entire
																		// response to the query
		return responseJSON;
	}

	/*
	 * Method takes in a Query-object and executes it to the Twitter API. Once the
	 * query is executed, the Twitter API returns the Tweet statuses in a series of
	 * batches. From each Tweet that is parsed, it is passed through a series of
	 * gateways to determine whether it will be archived and which TweetDataObject
	 * constructor to use to pass the data along. TweetDataObjects will contain all
	 * the objects relevant to be further parsed through and archived.
	 *
	 */
	public void parseTweetData(QueryObject query) {

		QueryObject referenceQuery = query; // creates a reference to the query parameter so it can be later modified
		JSONObject responseJSON = this.getAPIResponseJSONOb(query); // stores the full response to the query returned by
																	// API
		if (this.didAPIResponseHaveError(responseJSON)) {
			this.printTweetJSONPretty(responseJSON);
			return;
		}

		JSONArray responseElements = responseJSON.getJSONArray("results"); // Array of Tweet-metadata points
		String nextToken = null;

		for (int index = 0; index < responseElements.length(); index++) {
			JSONObject JSONTweet = responseElements.getJSONObject(index);

			// The following is a series of gateways to determine if the Tweet/Status object
			// should be archived.
			// It also determines which TweetDataObject constructor to use in order to
			// parse/archive data or
			// whether the the Twitter-user's bioLocation should be archived or retrieved
			// for processing.
			// I know it's ugly.
			// First check whether metadata is English/Spanish/French -> check if metadata
			// has embedded coordinate info -> (if it doesn't have embedded coord info) ->
			// check if user has bio location -> Check if bio-location contains ÜT-coords
			// which resembles user location -> (if the previous statement wasn't
			// applicable)
			// check whether bio-location coordinate data has been previously been archived
			// -> Last flow of logic: if bio-location wasn't archived, send location to
			// Geocoding API

			// *****************************************************************************************//BEGIN
			// Outer-Most IF
			if (this.doesTweetMetadataContainGeoLocation(JSONTweet) && this.isTweetCountryUSOrCA(JSONTweet)) {
				// creates a local TweetDataObject which will contain all relevant Tweet/Query
				// info to be archived
				TweetDataObject object1 = new TweetDataObject(referenceQuery, JSONTweet);
				archiver.archiveTweetMetadata(object1);
				System.out.println("A Tweet was archived. Coordinates were set via embedded metadata.");
			} else // if Tweet DOES NOT have coord. metadata
					// --------------------------------------------------------------------------------------------//BEGIN
					// OUTER ELSE
			if (this.doesUserHaveBioLocationInfo(JSONTweet)) { // BEGIN INNER IF, checks to see if user has a
																// bio-location
				String bioLocation = this.getUserBioLocationInfo(JSONTweet);
				Coordinates generatedCoords;
				if (doesBioLocationHaveUTCoordinates(bioLocation)) { // some users have "ÜT-coordinates as their
																		// bio-location, which resembles their actual
																		// location
					generatedCoords = this.extractCoordsFromUTBioLocation(bioLocation);
					TweetDataObject object3 = new TweetDataObject(referenceQuery, JSONTweet, generatedCoords);
					DataArchival.getInstance().archiveTweetMetadata(object3);
					System.out.println("A Tweet was archived. Coordinates were set via ÜT bio-location metadata  ");
					
				} else if (archiver.isLocationDataArchived(bioLocation)) {
					generatedCoords = DataArchival.getInstance().getCoordsFromArchivedLocationData(bioLocation);
					TweetDataObject tweet = new TweetDataObject(referenceQuery, JSONTweet, generatedCoords);
					DataArchival.getInstance().archiveTweetMetadata(tweet);
					System.out.println("A Tweet was archived. Coordinates were set via"
							+ " location data that was previously archived");
				}
				// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				else if (MapboxAPI.isLocationValidAndUSOrCA(bioLocation)) { // sends bio-location data to Google API,
																			// checks if its valid & within US/CA
					generatedCoords = MapboxAPI.getCoordinates(); // sets the coordinates that relate to the bioLocation
					DataArchival.getInstance().archiveLocationData(bioLocation, generatedCoords); // archives the bio-location-string and coords for future reference
					TweetDataObject object2 = new TweetDataObject(referenceQuery, JSONTweet, generatedCoords);
					DataArchival.getInstance().archiveTweetMetadata(object2);
					System.out.println("A Tweet was archived. Coordinates were set via Mapbox Geocoding API");
				}
			} // END INNER IF
			// *****************************************************************************************//END
			// Outer-Most-IF, END OUTER ELSE

			// The following conditional WILL NOT be entered if the API's response to the
			// query DOES NOT contain a "next" token
			if (index == (responseElements.length() - 1) && this.doesResponseContainNextToken(responseJSON)) {
				nextToken = this.getResponseNextToken(responseJSON);
				referenceQuery.setNextToken(nextToken); // query the query so the "next" request can be generated via
														// the token
				this.parseTweetData(referenceQuery); // recurses through the method once again with the updated query
			}

		} // end of 'for' loop

	}

	private boolean didAPIResponseHaveError(JSONObject APIResponse) {
		if (APIResponse.has("error")) { // the "error" key within the API-Response indicates that an error occurred
			return true;
		} else {
			return false;
		}
	}

	// if the Response-JSON contains a "next" key, that indicates that the query has
	// additional response datapoints
	// located within the next request. This method checks whether the API's
	// response (in the form of a JSONObject)
	// contains the token.
	private boolean doesResponseContainNextToken(JSONObject responseJSON) {
		if (responseJSON.has("next") && responseJSON.getString("next").equals(null) == false) {
			return true;
		} else {
			return false;
		}
	}

	private String getResponseNextToken(JSONObject responseJSON) {
		String nextToken = null;
		try {
			nextToken = responseJSON.getString("next");

			// Sometimes one to many '='randomly appears at the end of the token-string,
			// which is NOT correct and
			// screws up the execution of the utilization of getting the 'next' page of
			// results. Therefore, those
			// '=' bitch(es) needs to be sub-stringed out.
			while (nextToken.charAt(nextToken.length() - 1) == '=') {
				nextToken = nextToken.substring(0, nextToken.length() - 1);
			}
			return nextToken;
		} catch (Exception e) {
			System.out.println(e);
			return nextToken;
		}
	}

	public void printQueryResults(QueryObject query, int maxNumOfPrintedResults) throws TwitterException {

		JSONObject responseJSON = this.getAPIResponseJSONOb(query); // the full response returned by API
		if (this.didAPIResponseHaveError(responseJSON)) {
			this.printTweetJSONPretty(responseJSON);
			return;
		}

		JSONArray responseElements = responseJSON.getJSONArray("results"); // Array of Tweet-metadata points
		int count = 0;

		for (int index = 0; index < responseElements.length(); index++) {
			JSONObject JSONTweet = responseElements.getJSONObject(index);
			this.printTweetJSONPretty(JSONTweet);

			System.out.println("User Bio-location: " + this.getUserBioLocationInfo(JSONTweet));
			System.out.println(count);

			if (count == maxNumOfPrintedResults) { // checks to see if the number of results outputted matches the
													// maximum parameter
				return;
			}
			count++;
		} // end of 'for' loop

	}

	/*
	 * Method configures the connection to the Twitter API service using the unique
	 * access tokens and keys given to the developers. It returns a configured
	 * Twitter object in which all service calls to the API will be conducted
	 * through.
	 */
	private void configConnectionToAPI() {

		// Reads in the configuration.ini file to load the API authorization keys and
		// tokens
		try (FileReader fileReader = new FileReader("configuration.ini")) {
			Properties properties = new Properties();
			properties.load(fileReader);

			// authorization keys and tokens within configuration.ini are supplied by
			// Twitter Developer API/------------
			this.consumerKey = properties.getProperty("OAuthConsumerKey");
			this.consumerSecret = properties.getProperty("OAuthConsumerSecret");
			this.accessToken = properties.getProperty("OAuthAccessToken");
			this.accessTokenSecret = properties.getProperty("OAuthAccessTokenSecret");

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public int getMaxNumOfDataPointsPerRequest() {
		int numberFromFile = 0; // temporarily initialized
		// Reads in the configuration.ini file
		try (FileReader fileReader = new FileReader("configuration.ini")) {
			Properties properties = new Properties();
			properties.load(fileReader);

			// retrieves the maximum number of data points that can be retrieved per request
			// to the API
			numberFromFile = Integer.parseInt(properties.getProperty("MAX_NUM_OF_DATA_POINTS"));
			return numberFromFile;
		} catch (Exception e) {
			System.out.println(e);
		}
		return numberFromFile;
	}

	/*
	 * Method takes in a Tweet Status object and uses the Google.gson library to
	 * output the data in JSON format. This library worked much better than
	 * Twitter4J's JSON library
	 */
	public void printTweetJSONPretty(JSONObject tweetAsJSON) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String jsonString = gson.toJson(tweetAsJSON); // creates a JSON-formatted String from the Tweet status object
		System.out.println(jsonString);
	}

	/*
	 * When called, this method pauses the execution of the system. It gets the
	 * number seconds that the system needs to pause from the Twitter Search API.
	 * The number of seconds represents how much time needs to elapse until requests
	 * to the API can be made once again. In the case that more requests to the API
	 * are generated when it needs time to reset, an error would be thrown.
	 */
	private synchronized void pauseExecutionOfSystem() {
		// gets the number of seconds until reset-time from the API and adds 20 seconds
		// more for safety
		int secondsUntilReset = 300;
		System.out.println(
				"**System is now going to sleep for " + secondsUntilReset + " seconds until the API resets**" + "\n");
		try {
			TimeUnit.SECONDS.sleep(secondsUntilReset); // makes the whole system rest for the amount of time set by the
														// API until reset
			System.out.println("Rebooting now");
		} catch (InterruptedException e) {
			System.out.println(e);
		}
	}

	/*
	 * Method takes in a JSONObject representation of a Tweet/Status Object, parses
	 * it, and determines whether it is written in English or Spanish. It returns
	 * true if the "lang" key (ISO 639-1) contained within the JSON represents the
	 * language-code for English or Spanish or French and false otherwise. These
	 * languages were selected because they are the top 3 most common throughout the
	 * US/Canada. Also, other languages (e.g Chinese) would output through the
	 * Twitter4j library as "???".
	 */
	public boolean isTweetInEnglishOrSpanishOrFrench(JSONObject ob) {
		// System.out.println("Language: " +ob.get("lang")+ "\n"); //TESTING
		if (ob.get("lang").equals("en") || ob.get("lang").equals("es") || ob.get("lang").equals("fr")) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * This method first checks whether the Tweet (passed in as a representative
	 * JSONObject) contains geo-location metadata. If it contains geo-location
	 * metadata, the method will check whether the Tweet was shared in Canada or the
	 * United States. It returns true if it a Tweet that was shared within those
	 * countries. It returns false otherwise if the Tweet wasn't shared in those
	 * countries OR if it doesn't contain geo-coordinate metadata at all.
	 */
	public boolean isTweetCountryUSOrCA(JSONObject ob) {
		JSONObject placeData = new JSONObject();
		// System.out.println("Above Tweet has 'place:' " + ob.has("place")); // TESTING

		if (ob.has("place")) { // some Tweets have the "place" geo-coordinate key
			placeData = ob.getJSONObject("place"); // extracts the embedded user JSON data for place
			String countryCode = placeData.getString("country_code");
			// System.out.println("countryCode is "+ placeData.getString("countryCode"));
			// //TESTING

			if (countryCode.equalsIgnoreCase("US") || countryCode.equalsIgnoreCase("CA")) {
				return true;
			}

			else { // if it has a country-code but it's NOT US or CA
				return false;
			}

		}

		else { // if the Tweet contains NO geo-location data at all
				// System.out.println("The above Tweet DOES NOT contain place/location data");
			return false;
		}

	}

	/*
	 * Twitter has the functionality of allowing users to manually enter their
	 * location into their Twitter-bios. This method checks whether the user has
	 * entered any sort of data in there. Returns true if the user has entered some
	 * sort of information into there and false otherwise.
	 */
	public boolean doesUserHaveBioLocationInfo(JSONObject ob) {
		if (ob.has("user")) {
			JSONObject userData = ob.getJSONObject("user"); // extracts the user-data JSONObject
			String userBioLocation = userData.getString("location");
			if (userBioLocation.equals("") || userBioLocation.equals(null)) { // if the user's bio-location is empty
				return false;
			} else { // if the user has inputted data into their bio-location
				return true;
			}
		} else { // if the Tweet-object has no "user" metadata (which it always should)
			return false;
		}
	}

	public String getUserBioLocationInfo(JSONObject ob) {
		JSONObject userData = ob.getJSONObject("user"); // extracts the user-data JSONObject
		String userBioLocation = userData.getString("location");
		return userBioLocation;
	}

	public boolean doesTweetMetadataContainGeoLocation(JSONObject ob) {

		if (ob.has("place") == false) { // First level check to see if the Tweet-JSON even has a place key
			return false;
		}

		Object placeObject = ob.get("place"); // takes in the "place object", this is to test whether it's null or
												// contains data
		if (placeObject.equals(null)) {
			return false; // if the placeObject key is null, that means that there is no location data.
		}

		else {
			return true;
		}
	}

	/*
	 * ÜT stands for the Über Twitter Client. The numbers after it stand for the
	 * person's GPS coordinates for their location (latitude and longitude). SOME
	 * Twitter users have this, which depicts their approx. location coordinates.
	 * EXAMPLE: "ÜT: 10.485832,-66.898929". Third party apps such as Ubertwitter
	 * offer a service to have this set as their bio-location.
	 */
	public boolean doesBioLocationHaveUTCoordinates(String bioLocation) {
		String regex = "ÜT: [0-9].*";

		if (bioLocation.matches(regex)) {
			return true;
		} else {
			return false;
		}
	}

	public Coordinates extractCoordsFromUTBioLocation(String bioLocation) {
		Coordinates generatedCoords = new Coordinates();
		// will store the char index at which the comma is located, which seperates the
		// coordinate pair
		int indexOfCommaChar = 0;
		String latString;
		String longitString;

		// BY DEFAULT, the numbers (representing the lat & long)
		// WILL ALWAYS start at the 4th character index.
		for (int i = 4; i < bioLocation.length(); i++) {
			if (bioLocation.charAt(i) == ',') {
				indexOfCommaChar = i;
				break;
			}
		}

		try {
			latString = bioLocation.substring(4, indexOfCommaChar);
			longitString = bioLocation.substring(indexOfCommaChar + 1, bioLocation.length() - 1);
			generatedCoords.setLatitude(Double.parseDouble(latString));
			generatedCoords.setLongitude(Double.parseDouble(longitString));
		} catch (Exception e) { // this should never be reached due to the regex protection but just in case:
			generatedCoords.setLatitude(30.4383);
			generatedCoords.setLongitude(-84.2807);

		}
		return generatedCoords;

	}

	/*
	 * public static void main(String args[]) throws TwitterException {
	 * DataRetrieval retrieval = new DataRetrieval(); Query samplequery =
	 * retrieval.createQuery("#Metoo", "2018-12-10", "2018-12-14"); //yyyy-mm-dd
	 * format System.out.println(retrieval.getRemainingAmountOfRequests() +
	 * " requests remaining");
	 * System.out.println(retrieval.getSecondsUntilAPIReset()+
	 * " seconds until reset"); retrieval.parseTweetData(samplequery); }
	 */

}

// The Tweet ID is sequential, meaning the MOST RECENT Tweets has a larger ID
// than the older ones before it. The last (oldest) Tweet
// returned in each Result-batch has the smallest ID, therefore to get more
// Tweets with the same query, we need to set the ID of the
// last Tweet as the MaxID of the query.

/*
 * Important Links and notes
 * https://developer.twitter.com/en/docs/tweets/search/overview
 * https://developer.twitter.com/en/pricing
 * https://developer.twitter.com/en/docs/tweets/search/api-reference/premium-
 * search#Authentication First Geo-tagged Tweets: 11/19/2009 Profile Geo
 * enrichment metadata and filtering: 2/17/2015
 * https://developer.twitter.com/en/docs/tweets/search/api-reference/premium-
 * search.html#Authentication
 * 
 * 
 */