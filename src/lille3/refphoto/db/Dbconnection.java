package lille3.refphoto.db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Dbconnection {
	/**
     * Le logger.
     */
    private static Logger logger = Logger.getLogger(Dbconnection.class.getName());    
    private Connection con;
    private ResultSet rs;
    private ResultSetMetaData md;
    private Statement stmt;
    private String type_requete;
    //private PrintWriter out;
    private Driver driver;
    private String drivers = null;
    private String url = null;
    private String user = null;
    private String password = null;    
    
    public Dbconnection(Properties props) {
    	this.drivers = props.getProperty("drivers");
    	this.url = props.getProperty("refphoto.url");
    	this.user = props.getProperty("refphoto.user");
    	this.password = props.getProperty("refphoto.password");
    }       
   
    public void connect() {
        try {
        	Driver driver = (Driver) Class.forName(this.drivers).newInstance();
            DriverManager.registerDriver(driver);
            con=DriverManager.getConnection(this.url, this.user, this.password);            
        } catch(Exception e) {
            if (logger.isInfoEnabled())
            	logger.info("erreur: " + e.getMessage());       
        }
    }


    public ResultSet query(String query) {
        
	    if (con == null) {
	    	if (logger.isInfoEnabled())
	            logger.info("Impossible de se réaliser la requête, aucune connexion disponible");
	    	return null;
	    }
	
	    type_requete=type_requete.trim();
	    type_requete=query.substring(0,5);
		type_requete=type_requete.toUpperCase();
		
		try {
		        
			stmt = con.createStatement();
			if (type_requete.equals("UPDATE") || (type_requete.equals("INSERT"))) {
				stmt.executeUpdate(query);
				rs=null;
			} else {		        
			    rs = stmt.executeQuery(query);			    
			    md = rs.getMetaData();			    
			    stmt.close();
		    }
		} catch (SQLException e) {
			if (logger.isInfoEnabled())
				logger.info("erreur: " + e.getMessage());
	    }
	            
	            
		return rs;
	}
 
        
	public int getNbCols() {
	    int colonnes=0;
	    try {
	        colonnes = md.getColumnCount();        
	    } catch(SQLException e) {
	    	if (logger.isInfoEnabled())
	            logger.info("erreur: " + e.getMessage());
	    }
	    return colonnes;
	}


	public int getNbLines() {
	    int lignes=0;
	    try {
	        while(rs.next()) {
	            lignes++;
	        }
	    } catch(SQLException e) {
	    	if (logger.isInfoEnabled())
	            logger.info("erreur: " + e.getMessage());       
	    }
	    return lignes;
	}

	@Override
    public void finalize() {
    	this.disconnect();
    }

	public void disconnect() {
		try {
			rs.close();
		    con.close();            
		} catch(SQLException e) {
			if (logger.isInfoEnabled())
				logger.info("erreur: " + e.getMessage());       
		}
		try {
			DriverManager.deregisterDriver(this.driver);
		} catch(SQLException e) {
			if (logger.isInfoEnabled())
				logger.info("erreur: " + e.getMessage());
		}
	}
}