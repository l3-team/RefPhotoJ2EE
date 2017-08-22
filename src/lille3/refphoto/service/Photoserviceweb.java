package lille3.refphoto.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Properties;
import java.util.UUID;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import lille3.refphoto.security.Security;
import lille3.refphoto.utils.Sha1;
import lille3.refphoto.binarystore.Binarystore;
import lille3.refphoto.db.Dbconnectionmanager;
import lille3.refphoto.exception.NotFoundException;
import lille3.refphoto.ldap.Ldapconnection;
import lille3.refphoto.memcache.Memcache;


@Service
public class Photoserviceweb {
	
    private static Logger logger = Logger.getLogger(Photoserviceweb.class.getName());
	
	private Dbconnectionmanager connMgr;
	private Security secur;
	private Memcache mc;
	private Ldapconnection ldap;
	private Binarystore bs;
	
	
	
	public Photoserviceweb(ServletContext context) {
					
		connMgr = ((Dbconnectionmanager) context.getAttribute("dbRefphoto"));
		secur = ((Security) context.getAttribute("securRefphoto"));
		mc = ((Memcache) context.getAttribute("mcRefphoto"));
		ldap = ((Ldapconnection) context.getAttribute("ldapRefphoto"));
		bs = ((Binarystore) context.getAttribute("bsRefphoto"));
			
		if (logger.isInfoEnabled())
			logger.info("init photoservice");
		
	}
	
	public boolean checkValidServer(HttpServletRequest request) {
		
		
		boolean authorized = secur.checkAddressValidServer(request);
		
		if (authorized) {
			logger.info("this client is authorized");
		} else {
			logger.info("this client is not authorized");
		}
		
		return authorized;
	}
	
	public boolean checkXValidServer(HttpServletRequest request) {
		
		
		boolean authorized = secur.checkAddressXValidServer(request);
		
		if (authorized) {
			logger.info("this client is Xauthorized");
		} else {
			logger.info("this client is not Xauthorized");
		}
		
		return authorized;
	}
	
	/*public void requete() {
		
		Connection con = connMgr.getConnection("refphoto");
        if (con == null) {
        	if (logger.isInfoEnabled())
    			logger.info("Can't get connection");
            return;
        }
        ResultSet rs = null;
        ResultSetMetaData md = null;
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM sha1");
            md = rs.getMetaData();
            if (logger.isInfoEnabled())
    			logger.info("<H1>table sha1</H1>");
            while (rs.next()) {
            	if (logger.isInfoEnabled())
        			logger.info("<BR>");
                for (int i = 1; i <= md.getColumnCount(); i++) {
                	if (logger.isInfoEnabled())
            			logger.info(rs.getString(i) + ", ");
                }
            }
            stmt.close();
            rs.close();
        }
        catch (SQLException e) {
        	if (logger.isInfoEnabled())
    			logger.info("erreur:"+e.getMessage());
        }
        connMgr.freeConnection("refphoto", con);
		
	}*/
	
	public String createToken(HttpServletRequest request, String uid) {
		boolean verif_client_xvalid_server = secur.checkAddressXValidServer(request);
		String token = "";
		String bVerif = "1";
		
		if (verif_client_xvalid_server) {
			bVerif = "0";
		} else {
			bVerif = "1";
		}
		
		do {
			Sha1 s = new Sha1(uid + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(13).toLowerCase());
			token = s.getSha1().substring(0, 15);
		} while(this.mc.getTab("token_" + token) != null);
		
		String[] tab = new String[2];
		tab[0] = uid;
		tab[1] = bVerif;
				
		this.mc.setTab("token_"+token, 60*2, tab);		
		
		return token;
	}
	
	public String getPath(String token) {
		//logger.info("token="+token);
		String[] tab = this.mc.getTab("token_" + token);
		if (tab == null) throw new NotFoundException();
		String uid = tab[0];
		
		if ((uid.equals("")) || (uid == null)) throw new NotFoundException();
		String bVerif = tab[1];
		
		this.mc.delete("token_" + token);
		
		Sha1 sha1 = this.getSha1ForUid(uid);
		
		if (sha1 != null) {
			if (bVerif.equals("1")) {
				String query = "(&(objectclass=*)("+this.ldap.getFieldid()+"="+uid+"))";
				String[] attributes = { this.ldap.getFieldid(), this.ldap.getFieldname(), this.ldap.getFieldprofil(), this.ldap.getFieldprofils(), this.ldap.getFieldiddstudent(), this.ldap.getFieldidemployee() };
				this.ldap.Openconnection();
				Hashtable<String, String[]> results = this.ldap.search(query, attributes, "");
				this.ldap.Closeconnection();
				
				//String[] toto = results.get("supannEmpId");		
				//System.out.println("supannEmpId:"+toto[0]);
				String[] udllisteservices = results.get(this.ldap.getFieldname());
				if (udllisteservices == null) {
					return this.bs.getPathPhotoDefault();
				}
				if (udllisteservices[0].equals(this.ldap.getFieldnegativevalue())) {
					return this.bs.getPathPhotoBlocked();
				}
				if (udllisteservices[0].equals(this.ldap.getFieldpositivevalue())) {
					String[] buildpathwithsha1 = this.bs.buildPathWithSha1(sha1);
					return this.bs.getPhotoPath() + "/" + buildpathwithsha1[0] + "/" + buildpathwithsha1[1];
				}
			} else {
				String[] buildpathwithsha1 = this.bs.buildPathWithSha1(sha1);
				return this.bs.getPhotoPath() + "/" + buildpathwithsha1[0] + "/" + buildpathwithsha1[1];
			}
		}
		return this.bs.getPathPhotoBlocked();
		
	}
	
	public Sha1 getSha1ForUid(String uid) {
		Sha1 sha1 = this.mc.getSha1("sha1_" + uid);
		
		if (sha1 != null) return sha1;
		
		String query = "SELECT sha1 FROM sha1 WHERE uid = '" + uid + "'";
		
		Connection con = connMgr.getConnection("refphoto");
        if (con == null) {
        	if (logger.isInfoEnabled())
    			logger.info("Can't get connection");
            return null;
        }
        ResultSet rs = null;
        //ResultSetMetaData md = null;
        Statement stmt = null;
        String valeur = "";
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            //md = rs.getMetaData();                        
            if (rs.next()) {
            	valeur = rs.getString(1);                           
            } else {
            	valeur = "";
            }
            sha1 = new Sha1("");
            sha1.setSha1(valeur);
            stmt.close();
            rs.close();
        } catch (SQLException e) {
        	if (logger.isInfoEnabled())
    			logger.info("erreur:"+e.getMessage());
        }
        connMgr.freeConnection("refphoto", con);
		
        if (valeur.equals("")) return null;
        
        this.mc.setSha1("sha1_" + uid, 0, sha1);
        
		return sha1;
	}
	
	
	public void importUserPhoto(String uid, boolean force) {
		//String externalReference = "";
		String id = "";
		
		String query = "(&(objectclass=*)(" + this.ldap.getFieldid() + "=" + uid + "))";
		String[] attributes = { this.ldap.getFieldid(), this.ldap.getFieldname(), this.ldap.getFieldprofil(), this.ldap.getFieldprofils(), this.ldap.getFieldiddstudent(), this.ldap.getFieldidemployee() };		
		this.ldap.Openconnection();
		Hashtable<String, String[]> results = this.ldap.search(query, attributes, "");
		this.ldap.Closeconnection();
		
		//String[] toto = results.get("supannEmpId");		
		//logger.info("supannEmpId:"+toto[0]);
		
		if (results.isEmpty()) {
			return;
		}
		
		String[] type_personne = results.get(this.ldap.getFieldprofil());
		
		String[] tab_employee_values = this.ldap.getFieldemployeevalues().split(",");
		String[] tab_student_values = this.ldap.getFieldstudentvalues().split(",");
		boolean employee = ArrayUtils.contains(tab_employee_values, type_personne[0]);
		boolean student = ArrayUtils.contains(tab_student_values, type_personne[0]);
		
		if (employee) {
			String[] datas_employee = results.get(this.ldap.getFieldidemployee());
			if (datas_employee[0] == null) { 
				logger.info("Erreur : impossible de récupérer le numéro individu pour l'uid " + uid);
				System.out.println("Erreur : impossible de récupérer le numéro individu pour l'uid " + uid);
			}
			//externalReference = "0" + datas_employee[0];
			id = datas_employee[0];
		} else if (student) {
			String[] datas_student = results.get(this.ldap.getFieldiddstudent());
			if (datas_student[0] == null) { 
				logger.info("Erreur : impossible de récupérer le numéro étudiant pour l'uid " + uid);
				System.out.println("Erreur : impossible de récupérer le numéro étudiant pour l'uid " + uid);
			}
			//externalReference = datas_student[0];
			id = datas_student[0];
		} else {
			return;
		}
	}
	
	public String getUidByCodEtu(String codeetu) {
		String query = "(&(objectclass=*)("+this.ldap.getFieldiddstudent()+"="+codeetu+"))";
		String[] attributes = { this.ldap.getFieldid(), this.ldap.getFieldname(), this.ldap.getFieldprofil(), this.ldap.getFieldprofils(), this.ldap.getFieldiddstudent(), this.ldap.getFieldidemployee() };		
		this.ldap.Openconnection();
		Hashtable<String, String[]> results = this.ldap.search(query, attributes, "");
		this.ldap.Closeconnection();
		String[] uid = results.get(this.ldap.getFieldid());
		
		if (uid == null) {
			return "E";
		} else {
			return uid[0];
		}
		//return "E";
	}
	
	public String getUidByCodPers(String codepers) {
		String query = "(&(objectclass=*)("+this.ldap.getFieldidemployee()+"="+codepers+"))";
		String[] attributes = { this.ldap.getFieldid(), this.ldap.getFieldname(), this.ldap.getFieldprofil(), this.ldap.getFieldprofils(), this.ldap.getFieldiddstudent(), this.ldap.getFieldidemployee() };		
		this.ldap.Openconnection();
		Hashtable<String, String[]> results = this.ldap.search(query, attributes, "");
		this.ldap.Closeconnection();
		String[] uid = results.get(this.ldap.getFieldid());
		
		if (uid == null) {
			return "P";
		} else {
			return uid[0];
		}
		//return "P";
	}
	
	public void closeMc() {
		this.mc.closeConnection();
	}
}