package lille3.refphoto.ldap;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;


public class Ldapconnection {
	
	/**
     * Le logger.
     */
    private static Logger logger = Logger.getLogger(Ldapconnection.class.getName());
    
    private DirContext context = null;
    
    private Hashtable<String, String> environment;
    private String host;
    private String factoriesinitctx;
    private String factoriescontrol;
    private String authentication;
    private String user;
    private String password;
    private String basedn;
    private String branch;
    
    private String field_name;
    private String field_positivevalue;
    private String field_negativevalue;
    private String field_id;
    private String field_structureid;
    private String field_profil;
    private String field_profils;
    private String field_idstudent;
    private String field_idemployee;
    private String field_studentvalues;
    private String field_employeevalues;
    		
    public Ldapconnection(Properties props)
    {
    	environment = new Hashtable<String, String>();

    	this.host = props.getProperty("ldap.host");
    	this.factoriesinitctx = props.getProperty("ldap.factories.initctx");
    	this.factoriescontrol = props.getProperty("ldap.factories.control");
    	this.basedn = props.getProperty("ldap.basedn");
        this.branch = props.getProperty("ldap.branch");
        this.user = props.getProperty("ldap.user");
        this.password = props.getProperty("ldap.password");
        this.authentication = props.getProperty("ldap.authentication");
    	this.field_name = props.getProperty("ldap.field_name");
        this.field_positivevalue = props.getProperty("ldap.field_positivevalue");
        this.field_negativevalue = props.getProperty("ldap.field_negativevalue");
        this.field_id = props.getProperty("ldap.field_id");
        this.field_structureid = props.getProperty("ldap.field_structureid");
        this.field_profil = props.getProperty("ldap.field_profil");
        this.field_profils = props.getProperty("ldap.field_profils");
        this.field_idstudent = props.getProperty("ldap.field_idstudent");
        this.field_idemployee = props.getProperty("ldap.field_idemployee");
        this.field_studentvalues = props.getProperty("ldap.field_studentvalues");
        this.field_employeevalues = props.getProperty("ldap.field_employeevalues");
    	
    	environment.put(LdapContext.CONTROL_FACTORIES, this.factoriescontrol);    	
        environment.put(Context.INITIAL_CONTEXT_FACTORY, this.factoriesinitctx);        
        environment.put(Context.PROVIDER_URL, this.host);        
        environment.put(Context.SECURITY_AUTHENTICATION, this.authentication);        
        environment.put(Context.SECURITY_PRINCIPAL, this.user);        
        environment.put(Context.SECURITY_CREDENTIALS, this.password);                     
    }
    
    public void Openconnection()
    {
    	try 
        {
            context = new InitialDirContext(environment);
            if(logger.isInfoEnabled())
    			logger.info("Connecté au Ldap..");
            if(logger.isInfoEnabled())
    			logger.info(context.getEnvironment());
            
        } 
        catch (AuthenticationNotSupportedException e) 
        {
        	if(logger.isInfoEnabled())
    			logger.info("Authentification non supportée par le serveur:"+e.getMessage());
        }
        catch (AuthenticationException e)
        {
        	if(logger.isInfoEnabled())
    			logger.info("Utilisateur et/ou Mot de passe incorrect:"+e.getMessage());
        }
        catch (NamingException e)
        {
        	if(logger.isInfoEnabled())
    			logger.info("Erreur lors de la création de la connection Ldap");
        }
    }
	
    public Hashtable<String, String[]> search(String filter, String[] attributes, String overloadDn)
    {
    	Hashtable<String, String[]> resultats = new Hashtable<String, String[]>();
    	String searchBaseDn = "";
    	if (!overloadDn.equals(""))
    	{
    		searchBaseDn = overloadDn + "," + this.basedn;
    	} else
    	{
    		searchBaseDn = this.branch + "," + this.basedn;    	
    	}

    	try {
    		SearchControls searchControls = new SearchControls();
    		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    		searchControls.setReturningAttributes(attributes);
    		NamingEnumeration resultat = context.search(searchBaseDn, filter, searchControls);
    		
    		while (resultat.hasMore()) {
    			SearchResult sr = (SearchResult)resultat.next();
				Attributes attrs = sr.getAttributes();
				String at = "";
				String tmp = "";
				String[] tab_at = {};
				int idx = 0;
				int cpt = 0;
				NamingEnumeration ae = attrs.getAll();
				while(ae.hasMore()) {
					cpt = 0;
					idx = 0;
					Attribute attr = (Attribute) ae.next();
					at = attr.getID();
					logger.info("attribut:"+at);
					
					NamingEnumeration e = attr.getAll();
					while(e.hasMore()) {
						tmp = (String) e.next();
						cpt++;
					}
					e = attr.getAll();
					tab_at = new String[cpt];
					while(e.hasMore()) {			        	  
						tab_at[idx] = (String) e.next();
						logger.info(">"+tab_at[idx]);
						idx++;
					}
					resultats.put(at, tab_at);
				}
    		}
    		
    	} catch (NamingException e) {
    		if(logger.isInfoEnabled())
    			logger.info("erreur:"+e.getMessage());
    	}
    	
    	return resultats;
    }
    
    //@SuppressWarnings("unchecked")
	public Hashtable<String, String[]>[] searchMultiple(String filter, String[] attributes, String overloadDn)
    {    	
    	Hashtable<String, String[]> resultats = null;
    	Hashtable<String, String[]> tab_resultats[] = null;
    	
    	int compteur_resultats=0;    	
    	String searchBaseDn = "";
    	
    	if (!overloadDn.equals(""))
    	{
    		searchBaseDn = overloadDn + "," + this.basedn;
    	} else
    	{
    		searchBaseDn = this.branch + "," + this.basedn;    	
    	}

    	//System.out.println("searchBaseDn=["+searchBaseDn+"]");
    	//System.out.println("filter=["+filter+"]");
    	
    	try {
    		SearchControls searchControls = new SearchControls();
    		//searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    		searchControls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
    		searchControls.setReturningAttributes(attributes);
    		
    		//@SuppressWarnings("rawtypes")
			NamingEnumeration resultat = context.search(searchBaseDn, filter, searchControls);
    		
    		
    		while(resultat.hasMore()) {
    			SearchResult sr2 = (SearchResult)resultat.next();
    			compteur_resultats++;
    		}
    		
    		//System.out.println("compteur_resultats="+compteur_resultats);
    		
    		tab_resultats = new Hashtable[compteur_resultats];
			//resultats = new Hashtable<String, String[]>();
			compteur_resultats=-1;
			
    		resultat = context.search(searchBaseDn, filter, searchControls);
    		
    		while (resultat.hasMore()) {
    			resultats = new Hashtable<String, String[]>();
    			compteur_resultats++;
    			SearchResult sr = (SearchResult)resultat.next();
				Attributes attrs = sr.getAttributes();
				String at = "";
				//@SuppressWarnings("unused")
				String tmp = "";
				String[] tab_at = {};
				int idx = 0;
				int cpt = 0;
				//@SuppressWarnings("rawtypes")
				NamingEnumeration ae = attrs.getAll();
				while(ae.hasMore()) {
					cpt = 0;
					idx = 0;
					Attribute attr = (Attribute) ae.next();
					at = attr.getID();
					//System.out.println("attribut:"+at);
					
					//@SuppressWarnings("rawtypes")
					NamingEnumeration e = attr.getAll();
					while(e.hasMore()) {
						tmp = (String) e.next();
						cpt++;
					}
					e = attr.getAll();
					tab_at = new String[cpt];
					while(e.hasMore()) {			        	  
						tab_at[idx] = (String) e.next();
						//System.out.println(">"+tab_at[idx]);
						idx++;
					}
					resultats.put(at, tab_at);
				}
				tab_resultats[compteur_resultats] = resultats;
    		}
    		
    	} catch (NamingException e) {
    		if(logger.isInfoEnabled())
    			logger.info("erreur:"+e.getMessage());
    	}
    	
    	return tab_resultats;
    }
    
    public void Closeconnection()
    {
    	if (context != null)
    	{
    		try
    		{
				context.close();
				logger.info("Déconnecté du ldap");
				context = null;
			}
    		catch (NamingException e)
    		{
				if(logger.isInfoEnabled())
	    			logger.info("Erreur de fermeture de connection Ldap:"+e.getMessage());
			}
    	}
    }
    
    @Override
    public void finalize() {
    	this.Closeconnection();
    }
 
    public String getFieldname() {
    	return this.field_name;
    }
    
    public String getFieldpositivevalue() {
    	return this.field_positivevalue;
    }
    
    public String getFieldnegativevalue() {
    	return this.field_negativevalue;
    }
    
    public String getFieldid() {
    	return this.field_id;
    }
    public String getFieldstructureid() {
    	return this.field_structureid;
    }
    
    public String getFieldprofil() {
    	return this.field_profil;
    }
    
    public String getFieldprofils() {
    	return this.field_profils;
    }
    
    public String getFieldiddstudent() {
    	return this.field_idstudent;
    }
    
    public String getFieldidemployee() {
    	return this.field_idemployee;
    }
    
    public String getFieldstudentvalues() {
    	return this.field_studentvalues;
    }
    
    public String getFieldemployeevalues() {
    	return this.field_employeevalues;
    }
}