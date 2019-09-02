
/* This class serves to encapsulate data fields associated with the queries executed to
   the Twitter Premium Search Tweets API. */
public class QueryObject {

	private String queryStatement;
	private String fromDate;
	private String toDate;
	private String nextToken;
	
	public QueryObject(String queryStatement, String fromDate, String toDate) {
		this.queryStatement=queryStatement;
		this.fromDate=fromDate;
		this.toDate= toDate;
		this.nextToken=null;
		
	}
	
	public QueryObject(String queryStatement, String fromDate, String toDate, String nextToken) {
		this.queryStatement=queryStatement;
		this.fromDate=fromDate;
		this.toDate= toDate;
		this.nextToken=nextToken;
		
	}
	
	public String getQueryStatement() {
		return queryStatement;
	}
	
	public String getFromDate() {
		return this.fromDate;
	}
	
	public String getToDate() {
		return toDate;
	}
	
	public void setNextToken(String next) {
		this.nextToken= next;
	}
	
	public void setFromDate(String date) {
		this.fromDate= date;
	}
	
	public void setToDate(String date) {
		this.toDate= date;
	}
	
	/* The NEXT Token is provided by the API
	 * in order to point to the next set of 
	 * results in response to a query.
	 */
	public boolean hasNextToken() {
		if (this.nextToken!= null) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
	public String getNextToken() {
		if(this.nextToken==null) {
			System.out.println("THIS QUERY DOES NOT HAVE A NEXT TOKEN");
		}
		return this.nextToken;
	}
}
