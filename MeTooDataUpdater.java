

//@author John Neppel
	//SE Practicum 2018-2019

/*
This is a simple class that updates the database with the most 
current Tweets pertaining to a hashtag (#Metoo) that have been shared on Twitter.
 All extensions of the Tweet Trends Application, such as for the purpose of gathering daily
info on other keywords and hashtags can follow the same format.
*/
public class MeTooDataUpdater { 

	public static void main(String[] args)  { 
		TweetTrendsCore application = new TweetTrendsCore();
		application.archiveMostRecentTweets("#Metoo");
		application.closeDatabaseConnection(); //ensures that database connection has been closed
		System.exit(69); //to ensure all threads within the program are completely terminated
 
	}
  
} 
