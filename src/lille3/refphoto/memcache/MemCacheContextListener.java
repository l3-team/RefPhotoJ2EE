package lille3.refphoto.memcache;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.log4j.Logger;

import lille3.refphoto.db.Dbconnectionmanager;

/**
 * Classe permettant l'initialisation de log4j.
 * @author Fontaine Frederic.
 */
@WebListener
public final class MemCacheContextListener implements ServletContextListener {
	/**
     * Le logger.
     */
    private static Logger logger = Logger.getLogger(MemCacheContextListener.class.getName());

	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {
		System.out.println("MemCacheContextListener est en train d'initialiser Memcache.");
		ServletContext context = servletContextEvent.getServletContext();
		String prefix = context.getRealPath("/");
		String memcacheLocation = context.getInitParameter("memcache-properties-location");
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(prefix + memcacheLocation));
			Memcache mc = new Memcache(props);    		
            context.setAttribute("mcRefphoto", mc);
            if(logger.isInfoEnabled())
            	logger.info("L'initialisation du Memcache de l'application avec " + prefix + memcacheLocation + " s'est correctement déroulé.");	
		} catch (IOException e) {
			logger.error("L'initialisation du Memcache de l'application avec " + prefix + memcacheLocation + " s'est terminée en erreur.", e);
			
		}
	}

	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {
		if(logger.isInfoEnabled())
			logger.info("Fermeture du Memcache de l'application.");
		ServletContext context = servletContextEvent.getServletContext();

		Memcache mc = ((Memcache) context.getAttribute("mcRefphoto"));
		mc.closeConnection();
        //LogManager.shutdown();
	}
}