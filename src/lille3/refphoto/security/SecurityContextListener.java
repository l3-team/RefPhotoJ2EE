package lille3.refphoto.security;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.log4j.Logger;

/**
 * Classe permettant l'initialisation de log4j.
 * @author Fontaine Frederic.
 */
@WebListener
public final class SecurityContextListener implements ServletContextListener {
	/**
     * Le logger.
     */
    private static Logger logger = Logger.getLogger(SecurityContextListener.class.getName());

	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {
		System.out.println("SecurityContextListener est en train d'initialiser Security.");
		ServletContext context = servletContextEvent.getServletContext();
		String prefix = context.getRealPath("/");
		String securityLocation = context.getInitParameter("security-properties-location");
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(prefix + securityLocation));
			Security secur = new Security(props);
            context.setAttribute("securRefphoto", secur);
            if(logger.isInfoEnabled())
            	logger.info("L'initialisation du Security de l'application avec " + prefix + securityLocation + " s'est correctement déroulé.");	
		} catch (IOException e) {
			logger.error("L'initialisation du Security de l'application avec " + prefix + securityLocation + " s'est terminée en erreur.", e);
			
		}
	}

	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {
		if(logger.isInfoEnabled())
			logger.info("Fermeture du Binarystore de l'application.");
        //LogManager.shutdown();
	}
}