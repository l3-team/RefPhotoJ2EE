package lille3.refphoto.ldap;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.log4j.Logger;


/**
 * Classe permettant l'initialisation de ldap.
 * @author Mathieu Hétru.
 */
@WebListener
public final class LdapContextListener implements ServletContextListener {
	/**
     * Le logger.
     */
    private static Logger logger = Logger.getLogger(LdapContextListener.class.getName());

	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {
		ServletContext context = servletContextEvent.getServletContext();
		String prefix = context.getRealPath("/");
		String ldapConfigLocation = context.getInitParameter("ldap-properties-location");
    	try {
    		Properties props = new Properties();
    		props.load(new FileInputStream(prefix + ldapConfigLocation));
    		Ldapconnection ldapconn = new Ldapconnection(props);    		
            context.setAttribute("ldapRefphoto", ldapconn);
            if(logger.isInfoEnabled())
            	logger.info("L'initialisation de Ldap avec " + prefix + ldapConfigLocation + " s'est correctement déroulé.");
		}catch (final IOException e) {
			logger.error("L'initialisation de Ldap avec " + prefix + ldapConfigLocation + " s'est terminée en erreur.", e);
		}
	}

	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {
		if(logger.isInfoEnabled())
			logger.info("Fermeture de la connection Ldap de l'application.");

		//ServletContext context = servletContextEvent.getServletContext();
		//ldapconn.Closeconnection();
		//Ldapconnection ldapconn = ((Ldapconnection) context.getAttribute("ldapRefphoto"));
	}
}