package mapboxapiconnector;



import com.vdurmont.emoji.EmojiParser;
import twitter4j.JSONArray;
import twitter4j.JSONObject;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vdurmont.emoji.EmojiParser;

import geocodingGoogleAPI.Coordinates; 


public class ForwardGeocoderMapbox {

	private String accessToken;
	private static ForwardGeocoderMapbox instance;
	private JSONObject apiResponseObject; //this will hold the response of the API for other methods to utilize
	private static int numRequestsForMinute=0;
	
	//geocoding/v5/{endpoint}/{search_text}.json
	private final String GEOCODING_URL="https://api.mapbox.com/geocoding/v5/mapbox.places/{Query}.json?type=Place&access_token=";
	
	
	  
	  private ForwardGeocoderMapbox() {
		  try(FileReader fileReader = new FileReader("configuration.ini")) {
				Properties properties = new Properties();
				properties.load(fileReader);
				accessToken = properties.getProperty("MapboxAPIKey");
			}
			catch(Exception e) {
				System.out.println(e);
			}
	  } 
	  
	  
	  
	  public static ForwardGeocoderMapbox getInstance() {
		  if(instance==null) {
			  instance = new ForwardGeocoderMapbox();
		  }
		  return instance;
	  }
		  
	  private String getAPIResponseHttp(String locationQuery) {
		  this.nullifyAPIResponseObject();
		  String modifiedQuery="";
		  String responseString="";
		  HttpResponse httpResponse=null;
		  
		  //This will iterate through the characters of the location-query and
		  //check for spaces in it, which cannot be present within the URL going to the API.
		  //Spaces will be replaced with '%20', which represents a space
		  for(int i=0; i<locationQuery.length(); i++) {
			  if(locationQuery.charAt(i)== ' ' || locationQuery.charAt(i)=='|' || locationQuery.charAt(i)=='>') { 
				  modifiedQuery= modifiedQuery+ "%20";
			  }
			  else {
				  modifiedQuery= modifiedQuery + locationQuery.charAt(i);
			  }
		  }//end for loop
		  
		  //This will remove any emojis that may be present in the locationQuery.
		  modifiedQuery=EmojiParser.removeAllEmojis(modifiedQuery);
		  modifiedQuery.replaceAll("[-+.^:,]","");
		  
		  String mapboxQueryURL= GEOCODING_URL.replace("{Query}", modifiedQuery)+ this.accessToken;
		  try {
			  //System.out.println(mapboxQueryURL);
			  HttpGet httpGet = new HttpGet(mapboxQueryURL);
			  HttpClientBuilder builder = HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom()
	                .setCookieSpec(CookieSpecs.STANDARD).build());
	        
			  CloseableHttpClient httpClient = builder.build();
			
			  httpResponse = httpClient.execute(httpGet);
		
			  int statusCode = httpResponse.getStatusLine().getStatusCode();
			   //System.out.println(statusCode);
				HttpEntity entity = httpResponse.getEntity();
			
			  responseString = EntityUtils.toString(entity, "UTF-8");
			  httpClient.close();
				}
			
			catch(Exception e) {
				//System.out.println(e);
				//e.printStackTrace();
				JSONObject ob = new JSONObject();
				ob.put("Message", "An error has occurred with the query");
				responseString= ob.toString();
			}
			
			finally {
			
			}
		  return responseString;
	  }
	  
	  
	  
	  public boolean isLocationValidAndUSOrCA(String bioLocation) {
		 String apiResponseString= this.getAPIResponseHttp(bioLocation);
		 apiResponseObject= new JSONObject(apiResponseString);
		// System.out.println(apiResponseString);
		 
		 JSONArray contextArray;
		 JSONArray featuresArray;
		 
		//'features' JSON-Array contains info in response to the query. The
		// array ~should~ always be in the response JSON sent back by the API,
		 // however, it might be empty/ have a size of 0. The following checks for
		 //those conditions.
		if(apiResponseObject.has("features")==false || apiResponseObject.getJSONArray("features").length() ==0) {
			return false;
		}
		 featuresArray= apiResponseObject.getJSONArray("features");
	
		//the context JSONArray will contain info (e.g. coordinates, country)
		//about the location-query. The API's response may actually have many of
		//of these, however, the first one is the most accurate. This should
		//always be at the first index of the features Array, but it can vary.
	    contextArray=null;
		for(int j=0; j<featuresArray.length();j++) {
			if(featuresArray.getJSONObject(j).has("context")) {
				contextArray=featuresArray.getJSONObject(j).getJSONArray("context");
				break;
			}
			else { //if there is no 'features' array (which there always should be).
				return false;
			}
		}
		
		//The relevance field gives a measure of how accurate of a response is returned
		// by the API in reply to the bio-location passed into method. A low relevance likely
	    //means that the API's response is NOT likely an accurate represenation of the query's true location.
		if(featuresArray.getJSONObject(0).has("relevance")&& featuresArray.getJSONObject(0).getDouble("relevance")<0.54) {
			return false;
		} 
		
		 for(int i=0; i<contextArray.length();i++) {
			 JSONObject arrayElement = contextArray.getJSONObject(i);
			 String id = arrayElement.getString("id");
			 String regex = "country.*";
			 
			 //Attempts to find the array element that contains info on the geo-response's country
			 if(id.matches(regex)) { 
				 String countryCode= arrayElement.getString("short_code");
				 
				 //Checks whether the field resembles Canada or United States
				 if(countryCode.equalsIgnoreCase("us")|| countryCode.equalsIgnoreCase("ca")) {
					 return true;
				 }
				 
			 }
		 } //end of 'for' loop
		
		  return false;
		 
	  }
	  
	  @SuppressWarnings("unused")
	public Coordinates getCoordinates() {
		  Coordinates generatedCoords = new Coordinates();
		  JSONArray featuresArray= this.apiResponseObject.getJSONArray("features");
		  
		  JSONArray centerLocationCoords= featuresArray.getJSONObject(0).getJSONArray("center");
		  generatedCoords.setLongitude(centerLocationCoords.getDouble(0));
		  generatedCoords.setLatitude(centerLocationCoords.getDouble(1));
		  this.nullifyAPIResponseObject(); //ENSURES that the response-holder is clear for the next API-call.
		  return generatedCoords;
	  }
	  
	  
	  /* Method sets the apiResponseObject instance variable
	   * to null. This ensures that the variable is clear
	   * prior to another call to the Geocoding API is made.
	   */
	  private void nullifyAPIResponseObject() {
		  this.apiResponseObject=null;
	  }
	       
	   public static void main(String[]args) {
		   ForwardGeocoderMapbox api = new ForwardGeocoderMapbox();
		  
		   System.out.println(api.isLocationValidAndUSOrCA("El Paz"));
		   
	   }
	  
	  
	  
		}

