package lille3.refphoto.logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Classe permettant l'initialisation de log4j.
 * @author Fontaine Frederic.
 */
@WebListener
public final class Log4jContextListener implements ServletContextListener {
	/**
     * Le logger.
     */
    private static Logger logger = Logger.getLogger(Log4jContextListener.class.getName());

	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {
		System.out.println("Log4jContextListener est en train d'initialiser log4j.");
		ServletContext context = servletContextEvent.getServletContext();
		String prefix = context.getRealPath("/");
		String log4jLocation = context.getInitParameter("log4j-properties-location");
		if(log4jLocation != null && prefix != null){
			PropertyConfigurator.configure(prefix + log4jLocation);
			if(logger.isInfoEnabled())
				logger.info("Initialisation de Log4j avec " + prefix + log4jLocation + " s'est correctement déroulé.");
		} else {
			System.out.println("Log4J n'est pas configuré: " + prefix + log4jLocation);
	    }
	}

	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {
		if(logger.isInfoEnabled())
			logger.info("Fermeture des connections Log4j de l'application.");
        LogManager.shutdown();
	}
}