package lille3.refphoto.binarystore;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.log4j.Logger;
import lille3.refphoto.logger.Log4jContextListener;

/**
 * Classe permettant l'initialisation de log4j.
 * @author Fontaine Frederic.
 */
@WebListener
public final class BinaryStoreContextListener implements ServletContextListener {
	/**
     * Le logger.
     */
    private static Logger logger = Logger.getLogger(Log4jContextListener.class.getName());

	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {
		System.out.println("BinaryStoreContextListener est en train d'initialiser Binarystore.");
		ServletContext context = servletContextEvent.getServletContext();
		String prefix = context.getRealPath("/");
		String binarystoreLocation = context.getInitParameter("binarystore-properties-location");
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(prefix + binarystoreLocation));
			Binarystore bs = new Binarystore(props);    		
            context.setAttribute("bsRefphoto", bs);
            if(logger.isInfoEnabled())
            	logger.info("L'initialisation du binarystore de l'application avec " + prefix + binarystoreLocation + " s'est correctement déroulé.");	
		} catch (IOException e) {
			logger.error("L'initialisation du binarystore de l'application avec " + prefix + binarystoreLocation + " s'est terminée en erreur.", e);
			
		}
	}

	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {
		if(logger.isInfoEnabled())
			logger.info("Fermeture du Binarystore de l'application.");
        //LogManager.shutdown();
	}
}