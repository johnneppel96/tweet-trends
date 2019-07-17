import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;

public class KeepAliveThread extends Thread {
	private ArrayList<Connection> connections;

	KeepAliveThread(ArrayList<Connection> connections) {
		super();
		this.connections = connections;
	}

	public void run() {
		PreparedStatement stmt = null;
		while (!interrupted()) {
			
			//iterates through the arraylist of connections to execute a 'keep alive' query for each
			for (int i = 0; i < connections.size(); i++) {
				try {
					sleep(4350);
					stmt = connections.get(i).prepareStatement("do 1");
					stmt.execute();
				} catch (InterruptedException e) {
					return;
				} catch (Exception sqle) {
					/*
					 * This thread should run even on DB error. If autoReconnect is true, the
					 * connection will reconnect when the DB comes back up.
					 */
				} finally {
	                if(i==(connections.size()-1)) {
	                	i=0; //resets the for loop counter
	                }
					if (stmt != null) {
						try {
							stmt.close();
						} catch (Exception e) {
						}
					}
					stmt = null;
				}
			} //end of FOR
		} //end of WHILE
	}
}