package lille3.refphoto.db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;


/**
 * This class is a Singleton that provides access to one or many
 * connection pools defined in a Property file. A client gets
 * access to the single instance through the static getInstance()
 * method and can then check-out and check-in connections from a pool.
 * When the client shuts down it should call the release() method
 * to close all open connections and do other clean up.
 */
public class Dbconnectionmanager {
	
	/**
     * Le logger.
     */
    private static Logger logger = Logger.getLogger(Dbconnectionmanager.class.getName());
	
    static private Dbconnectionmanager instance;       // The single instance
    static private int clients;

    private Vector drivers = new Vector();
    private Hashtable pools = new Hashtable();
    private int sgbdr = 0;

    /**
     * Returns the single instance, creating one if it's the
     * first time this method is called.
     *
     * @return Dbconnectionmanager The single instance.
     */
    public static synchronized Dbconnectionmanager getInstance(Properties props) {
        if (instance == null) {
            instance = new Dbconnectionmanager(props);
        }
        clients++;
        return instance;
    }
        
    /**
     * A private constructor since this is a Singleton
     */
    private Dbconnectionmanager(Properties props) {
        init(props);
    }

    /**
     * Returns a connection to the named pool.
     *
     * @param name The pool name as defined in the properties file
     * @param con The Connection
     */
    public void freeConnection(String name, Connection con) {
    	Dbconnectionpool pool = (Dbconnectionpool) pools.get(name);
        if (pool != null) {
            pool.freeConnection(con);
        }
    }

    /**
     * Returns an open connection. If no one is available, and the max
     * number of connections has not been reached, a new connection is
     * created.
     *
     * @param name The pool name as defined in the properties file
     * @return Connection The connection or null
     */
    public Connection getConnection(String name) {
    	Dbconnectionpool pool = (Dbconnectionpool) pools.get(name);
        if (pool != null) {
            return pool.getConnection();
        }
        return null;
    }

    /**
     * Returns an open connection. If no one is available, and the max
     * number of connections has not been reached, a new connection is
     * created. If the max number has been reached, waits until one
     * is available or the specified time has elapsed.
     *
     * @param name The pool name as defined in the properties file
     * @param time The number of milliseconds to wait
     * @return Connection The connection or null
     */
    public Connection getConnection(String name, long time) {
    	Dbconnectionpool pool = (Dbconnectionpool) pools.get(name);
        if (pool != null) {
            return pool.getConnection(time);
        }
        return null;
    }

    /**
     * Closes all open connections and deregisters all drivers.
     */
    public synchronized void release() {
        // Wait until called by the last client
        if (--clients != 0) {
            return;
        }

        Enumeration allPools = pools.elements();
        while (allPools.hasMoreElements()) {
        	Dbconnectionpool pool = (Dbconnectionpool) allPools.nextElement();
            pool.release();
        }
        Enumeration allDrivers = drivers.elements();
        while (allDrivers.hasMoreElements()) {
            Driver driver = (Driver) allDrivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                if (logger.isInfoEnabled())
        			logger.info("Deregistered JDBC driver " + driver.getClass().getName());
            }
            catch (SQLException e) {
            	if (logger.isInfoEnabled())
        			logger.info("Can't deregister JDBC driver: " + driver.getClass().getName()+":"+e.getMessage());
            }
        }
    }

    /**
     * Creates instances of DBConnectionPool based on the properties.
     * A DBConnectionPool can be defined with the following properties:
     * <PRE>
     * &lt;poolname&gt;.url         The JDBC URL for the database
     * &lt;poolname&gt;.user        A database user (optional)
     * &lt;poolname&gt;.password    A database user password (if user specified)
     * &lt;poolname&gt;.maxconn     The maximal number of connections (optional)
     * </PRE>
     *
     * @param props The connection pool properties
     */
    private void createPools(Properties props) {
        Enumeration propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String name = (String) propNames.nextElement();
            if (name.endsWith(".url")) {
                String poolName = name.substring(0, name.lastIndexOf("."));
                String url = props.getProperty(poolName + ".url");
                if (url == null) {
                	if (logger.isInfoEnabled())
            			logger.info("No URL specified for " + poolName);
                    continue;
                }
                String user = props.getProperty(poolName + ".user");
                String password = props.getProperty(poolName + ".password");
                String maxconn = props.getProperty(poolName + ".maxconn", "0");
                int max;
                try {
                    max = Integer.valueOf(maxconn).intValue();
                }
                catch (NumberFormatException e) {
                	if (logger.isInfoEnabled())
            			logger.info("Invalid maxconn value " + maxconn + " for " + poolName);
                    max = 0;
                }
                Dbconnectionpool pool = new Dbconnectionpool(poolName, url, user, password, max);
                pools.put(poolName, pool);
                if (logger.isInfoEnabled())
        			logger.info("Initialized pool " + poolName);
            }
        }
    }

    /**
     * Loads properties and initializes the instance with its values.
     */
    private void init(Properties props) {
        loadDrivers(props);
        createPools(props);
    }

    /**
     * Loads and registers all JDBC drivers. This is done by the
     * Dbconnectionmanager, as opposed to the DBConnectionPool,
     * since many pools may share the same driver.
     *
     * @param props The connection pool properties
     */
    private void loadDrivers(Properties props) {
        String driverClasses = props.getProperty("drivers");
        StringTokenizer st = new StringTokenizer(driverClasses);
        while (st.hasMoreElements()) {
            String driverClassName = st.nextToken().trim();
            try {
                Driver driver = (Driver)
                    Class.forName(driverClassName).newInstance();
                DriverManager.registerDriver(driver);
                drivers.addElement(driver);
                if (logger.isInfoEnabled())
        			logger.info("Registered JDBC driver " + driverClassName);
            }
            catch (Exception e) {
            	if (logger.isInfoEnabled())
        			logger.info("Can't register JDBC driver: " +
                    driverClassName + ", Exception: " + e.getMessage());
            }
        }
    }

    

    
}