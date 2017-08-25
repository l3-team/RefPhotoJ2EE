package lille3.refphoto.db;



import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.log4j.Logger;

/**
 * Listener permetant l'initialisaton du pool de connexion à la base Oracle.
 * @author Fontaine Frederic.
 */
@WebListener
public final class DatabaseContextListener implements ServletContextListener {
	/**
	 * Le logger.
	 */
	private static Logger logger = Logger.getLogger(DatabaseContextListener.class.getName());

	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {
		ServletContext context = servletContextEvent.getServletContext();
		String prefix = context.getRealPath("/");
		String dbConfigLocation = context.getInitParameter("database-properties-location");
    	try {
    		Properties props = new Properties();
    		props.load(new FileInputStream(prefix + dbConfigLocation));
    		Dbconnectionmanager connMgr = Dbconnectionmanager.getInstance(props);
    		Dbconnection db = new Dbconnection(props);
            context.setAttribute("dbRefphoto", connMgr);
            context.setAttribute("dbRefphoto2", db);
            if(logger.isInfoEnabled())
            	logger.info("L'initialisation du pool de connections de l'application avec " + prefix + dbConfigLocation + " s'est correctement déroulé.");
		}catch (final IOException e) {
			logger.error("L'initialisation du pool de connections de l'application avec " + prefix + dbConfigLocation + " s'est terminée en erreur.", e);
		}
	}

	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {
		if(logger.isInfoEnabled())
			logger.info("Fermeture du pool de connections de l'application.");
		ServletContext context = servletContextEvent.getServletContext();

		Dbconnectionmanager connMgr = ((Dbconnectionmanager) context.getAttribute("dbRefphoto"));
		connMgr.release();
	}
}