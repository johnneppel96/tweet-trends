import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.Query;
import twitter4j.TwitterException;

import java.sql.SQLException;
import java.util.*;

//@author John Neppel
	//SE Practicum 2018-2019
/*
This class serves as a wrapper for all of the classes that make up
* the Tweet Trends Application. Aside from wrapping all of the classes
* together, it also controls the inputs that get forwarded and processed
* by the other classes and has the other classes retrieve/archive
* Tweet data in a particular manner (e.g archive most recent Tweets, archive
* Tweets over a specified timespan)
* */

public class TweetTrendsCore {

	private DataRetrievalHttp retriever;
	private final int assignedDBPoolIndex=1;
	
	public TweetTrendsCore() {
		retriever = new DataRetrievalHttp();
	}
	
	
	/* Method will archive the most recently shared Tweets on Twitter pertaining to the query,
	 * up until it parses a Tweet that has an ID less than or equal to the ID of the most recently
	 * archived Tweet located within the database.
	 */
	public void archiveRangeOfTweets(String queryStatement, String sinceDate, String untilDate) {
		if(DataArchival.getInstance().isDatabaseConnectionActive()==false) {
			this.printDatabaseConnectionError();
			return;
		}
		
		if(this.isDateValidFormat(sinceDate)==false|| this.isDateValidFormat(untilDate)==false) {
			System.out.println("Date is NOT in yyyymmddhhmm format; no metadata will be parsed or archived from the entered query");
			return;
		}
		QueryObject generatedQuery= new QueryObject(queryStatement, sinceDate, untilDate);
		try {
			retriever.parseTweetData(generatedQuery);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	/*Ensures that the date is in yyyymmddhhmm format. Returns true if parameter passed in is in that format
	 *and false otherwise.
	 */
	public boolean isDateValidFormat(String inputDate) {
		
		// regular expression to test any date with yyyymmddhhmm format
	    //entered dates that DO NOT exist
		// (e.g 201502336969) queried to the
		// API simply returns NULL results
		Pattern p = Pattern.compile("[0-9][0-9][0-9][0-9]" //yyyy
				+ "[0-9][0-9][0-9][0-9]" //mmdd
				+ "[0-9][0-9][0-9][0-9]");//hhmm

		Matcher m = p.matcher(inputDate); // tests the user input against the regular expression
		boolean doesItMatch= m.matches();
		
			if (doesItMatch==true) { // tests the user input against the regex
				return true;
			}
			else {
				return false;
			}

		} 
	
	
	public java.util.Calendar getTomorrowsDate() {
		Calendar tomorrowsDate = Calendar.getInstance();
	    tomorrowsDate.add(Calendar.DATE, 1); //advances to the date of one day later than the current date
	    
	    return tomorrowsDate;
	}
	
	
	public java.util.Calendar getDateFromMonthAgo() {
		Calendar oneMonthAgosDate = Calendar.getInstance();
	    oneMonthAgosDate.add(Calendar.MONTH, -1); //subtracts one month from the current Date
	    
	  return oneMonthAgosDate;
	}
	
	
	/*Method takes in a Calendar object and returns a representative string in yyyymmddhhmm format.
	 * This is to ensure that all generated dates are in that format in order to effectively query the
	 * Twitter API.
	 */
	public String convertDateToFormattedString(Calendar date) {
		String formattedDate="";
		//Java returns the numerical value associated with each month by the Gregorian and Julian standard.
		//This means that the numerical value of each month is ONE-less than our traditional calendar.
		int month = date.get(Calendar.MONTH)+1; //Therefore, add one to make up for the deficit
		String monthString= month+ "";
		
		int dayOfMonth= date.get(Calendar.DAY_OF_MONTH);
		String dayOfMonthString=dayOfMonth+"";
		
		if(month<10) { // Any Date values less than 10 are given a 0 before it (part of formatting)
			monthString= "0"+ month;
		}
		
		if(dayOfMonth<10) {
			dayOfMonthString="0"+ dayOfMonthString;
		}
		
		//yyyymmddmmss formatted string
		formattedDate= date.get(Calendar.YEAR) + monthString+ dayOfMonthString+"0000";
		
		return formattedDate;
	}
	
	
	public void printSomeQueryResults(String queryStatement, String sinceDate, String untilDate, int amountPrinted) {
		if(this.isDateValidFormat(sinceDate)==false|| this.isDateValidFormat(untilDate)==false) {
			System.out.println("Date is NOT in yyyymmddhhmm format; no metadata will be printed from the entered query");
			return;
		}
		
		QueryObject createdQuery = new QueryObject(queryStatement, sinceDate, untilDate); //creates a Query object from parameters
		try {
		retriever.printQueryResults(createdQuery, amountPrinted);
		}
		catch(Exception e) {
			System.out.println(e);
		}
	}
	
	
	
	private void printDatabaseConnectionError() {
		System.out.println("Not connected to the database; no data will be parsed"
				+ " or archived");
	}
	
	public void closeDatabaseConnections() {
		DataArchival.getInstance().closeDatabaseConnection();
	}
	
	

}
