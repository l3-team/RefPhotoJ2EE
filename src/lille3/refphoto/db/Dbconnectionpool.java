package lille3.refphoto.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * This inner class represents a connection pool. It creates new
 * connections on demand, up to a max number if specified.
 * It also makes sure a connection is still open before it is
 * returned to a client.
 */
public class Dbconnectionpool {
	/**
     * Le logger.
     */
    private static Logger logger = Logger.getLogger(Dbconnectionpool.class.getName());
	
    private int checkedOut;
    private Vector freeConnections = new Vector();
    private int maxConn;
    private String name;
    private String password;
    private String URL;
    private String user;

    /**
     * Creates new connection pool.
     *
     * @param name The pool name
     * @param URL The JDBC URL for the database
     * @param user The database user, or null
     * @param password The database user password, or null
     * @param maxConn The maximal number of connections, or 0
     *   for no limit
     */
    public Dbconnectionpool(String name, String URL, String user, String password, int maxConn) {
        this.name = name;
        this.URL = URL;
        this.user = user;
        this.password = password;
        this.maxConn = maxConn;
    }

    /**
     * Checks in a connection to the pool. Notify other Threads that
     * may be waiting for a connection.
     *
     * @param con The connection to check in
     */
    public synchronized void freeConnection(Connection con) {
        // Put the connection at the end of the Vector
        if (con != null) {
          freeConnections.addElement(con);
          checkedOut--;
          notifyAll();
        }
    }


    /**
     * Checks out a connection from the pool. If no free connection
     * is available, a new connection is created unless the max
     * number of connections has been reached. If a free connection
     * has been closed by the database, it's removed from the pool
     * and this method is called again recursively.
     */
    public synchronized Connection getConnection() {
        Connection con = null;
        if (freeConnections.size() > 0) {
            // Pick the first Connection in the Vector
            // to get round-robin usage
            con = (Connection) freeConnections.firstElement();
            freeConnections.removeElementAt(0);
            try {
                if (con.isClosed()) {
                	if (logger.isInfoEnabled())
            			logger.info("Removed bad connection from " + name);
                    // Try again recursively
                    con = getConnection();
                }
            }
            catch (SQLException e) {
            	if (logger.isInfoEnabled())
        			logger.info("Removed bad connection from " + name);
                // Try again recursively
                con = getConnection();
            }
        }
        else if (maxConn == 0 || checkedOut < maxConn) {
            con = newConnection();
        }
        if (con != null) {
            checkedOut++;
        }
        return con;
    }

    /**
     * Checks out a connection from the pool. If no free connection
     * is available, a new connection is created unless the max
     * number of connections has been reached. If a free connection
     * has been closed by the database, it's removed from the pool
     * and this method is called again recursively.
     * <P>
     * If no connection is available and the max number has been
     * reached, this method waits the specified time for one to be
     * checked in.
     *
     * @param timeout The timeout value in milliseconds
     */
    public synchronized Connection getConnection(long timeout) {
        long startTime = new Date().getTime();
        Connection con;
        while ((con = getConnection()) == null) {
            try {
                wait(timeout);
            }
            catch (InterruptedException e) {}
            if ((new Date().getTime() - startTime) >= timeout) {
                // Timeout has expired
                return null;
            }
        }
        return con;
    }

    /**
     * Closes all available connections.
     */
    public synchronized void release() {
        Enumeration allConnections = freeConnections.elements();
        while (allConnections.hasMoreElements()) {
            Connection con = (Connection) allConnections.nextElement();
            try {
                con.close();
                if (logger.isInfoEnabled())
        			logger.info("Closed connection for pool " + name);
            }
            catch (SQLException e) {
            	if (logger.isInfoEnabled())
        			logger.info("Can't close connection for pool " + name + ":"+e.getMessage());
            }
        }
        freeConnections.removeAllElements();
    }

    /**
     * Creates a new connection, using a userid and password
     * if specified.
     */
    private Connection newConnection() {
        Connection con = null;
        try {
            if (user == null) {
                con = DriverManager.getConnection(URL);
            }
            else {
                con = DriverManager.getConnection(URL, user, password);
            }
            if (logger.isInfoEnabled())
    			logger.info("Created a new connection in pool " + name);
        }
        catch (SQLException e) {
        	if (logger.isInfoEnabled())
    			logger.info("Can't create a new connection for " + URL + ":"+e.getMessage());
            return null;
        }
        return con;
    }
}