package lille3.refphoto.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Service;

import lille3.refphoto.binarystore.Binarystore;
import lille3.refphoto.db.Dbconnection;
import lille3.refphoto.ldap.Ldapconnection;
import lille3.refphoto.memcache.Memcache;
import lille3.refphoto.utils.Sha1;


@Service
public class Photoservicecli {
	
	//private Dbconnectionmanager connMgr;
	private Dbconnection db;
	private Ldapconnection ldap;
	private Memcache mc;
	private Binarystore bs;
	
	
	public Photoservicecli() {
		
		try {
    		Properties props = new Properties();
    		props.load(new FileInputStream("../conf/database.properties"));
    		//connMgr = Dbconnectionmanager.getInstance(props);
    		db = new Dbconnection(props);
            
    		//props = new Properties();
    		//props.load(new FileInputStream("../conf/security.properties"));
    		//secur = new Security(props);
    		
    		props = new Properties();
    		props.load(new FileInputStream("../conf/memcache.properties"));
    		mc = new Memcache(props);
    		
    		props = new Properties();
    		props.load(new FileInputStream("../conf/ldap.properties"));
    		ldap = new Ldapconnection(props);
    		
    		props = new Properties();
    		props.load(new FileInputStream("../conf/binarystore.properties"));
    		bs = new Binarystore(props);
    		
		} catch (IOException e) {
			System.out.println("Erreur:" + e.getMessage());
		}
			
			
			
			
		//System.out.println("init photoservice");
		
	}
	
	
	
	
	/*public void requete() {
		
		Connection con = connMgr.getConnection("refphoto");
        if (con == null) {
        	System.out.println("Can't get connection");
            return;
        }
        ResultSet rs = null;
        ResultSetMetaData md = null;
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM sha1");
            md = rs.getMetaData();
            System.out.println("<H1>table sha1</H1>");
            while (rs.next()) {
            	System.out.println("<BR>");
                for (int i = 1; i <= md.getColumnCount(); i++) {
                	System.out.println(rs.getString(i) + ", ");
                }
            }
            stmt.close();
            rs.close();
        }
        catch (SQLException e) {
        	System.out.println("erreur:"+e.getMessage());
        }
        connMgr.freeConnection("refphoto", con);
		
	}*/
	
	
	
	/*public String getPath(String token) {
		String[] tab = this.mc.getTab("token_" + token);
		String uid = tab[0];
		
		String bVerif = tab[1];
		
		this.mc.delete("token_" + token);
		
		Sha1 sha1 = this.getSha1ForUid(uid);
		
		if (sha1 != null) {
			String query = "";
		}
		
		return "";
		
	}*/
	
	public Sha1 getSha1ForUid(String uid) {
		Sha1 sha1 = this.mc.getSha1("sha1_" + uid);
		
		if (sha1 != null) return sha1;
		
		String query = "SELECT sha1 FROM sha1 WHERE uid = '" + uid + "'";
		
		//Connection con = connMgr.getConnection("refphoto");
		this.db.connect();
        //if (con == null) {
		if (!this.db.isConnected()) {
        	System.out.println("Can't get connection");
            return null;
        }
        ResultSet rs = null;
        //ResultSetMetaData md = null;
        //Statement stmt = null;
        String valeur = "";
        try {        	
            //stmt = con.createStatement();
            //rs = stmt.executeQuery(query);
        	rs = this.db.query(query);
            //md = rs.getMetaData();                        
            if (rs.next()) {
            	valeur = rs.getString(1);                           
            } else {
            	valeur = "";
            }
            //stmt.close();
            //rs.close();
        } catch (SQLException e) {
        	System.out.println("erreur:"+e.getMessage());
        }
        //connMgr.freeConnection("refphoto", con);
        this.db.disconnect();
		
        if (valeur.equals("")) return null;
        
        this.mc.setSha1("sha1_" + uid, 0, new Sha1(valeur));
        
		return new Sha1(valeur);
	}
	
	public void importAllPhoto() {
		String query = "(&(objectclass=*)(" + this.ldap.getFieldid() + "=*))";
		
		//String query = this.ldap.getFieldid() + "=1940";
		
		String[] attributes = { this.ldap.getFieldid() };		
		this.ldap.Openconnection();
		Hashtable<String, String[]> results[] = this.ldap.searchMultiple(query, attributes, "");
		//Hashtable<String, String[]> results = this.ldap.search(query, attributes, "");
		this.ldap.Closeconnection();
		
		
		/*if (results.isEmpty()) {
			return;
		}
		
		String[] uid = results.get(this.ldap.getFieldid());
		
		System.out.println("uid=["+uid[0]+"]");*/
		
		String[] ids = null;
		
		if (results.length == 0) {
			return;
		}
		for(int i=0; i<results.length;i++) {
			ids = results[i].get(this.ldap.getFieldid());
			//for(int j=0; j<ids.length; j++) {
				//System.out.println("ids[0]=["+ids[0]+"]");
				this.importUserPhoto(ids[0]);
			//}
		}
		
	}
	
	public void importUserPhoto(String uid) {
		//String externalReference = "";
		String id = "";
		
		String query = "(&(objectclass=*)(" + this.ldap.getFieldid() + "=" + uid + "))";
		String[] attributes = { this.ldap.getFieldid(), this.ldap.getFieldname(), this.ldap.getFieldprofil(), this.ldap.getFieldprofils(), this.ldap.getFieldiddstudent(), this.ldap.getFieldidemployee() };		
		this.ldap.Openconnection();
		Hashtable<String, String[]> results = this.ldap.search(query, attributes, "");
		this.ldap.Closeconnection();
		
		//String[] toto = results.get("supannEmpId");		
		//System.out.println("supannEmpId:"+toto[0]);
		String[] profil = results.get(this.ldap.getFieldprofil());
		
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
			if (datas_employee == null) { 
				System.out.println("Erreur : impossible de récupérer le numéro individu pour l'uid " + uid);
				return;
			}
			//externalReference = "0" + datas_employee[0];
			id = datas_employee[0];
		} else if (student) {
			String[] datas_student = results.get(this.ldap.getFieldiddstudent());
			if (datas_student == null) { 
				System.out.println("Erreur : impossible de récupérer le numéro étudiant pour l'uid " + uid);
				return;
			}
			//externalReference = datas_student[0];
			id = datas_student[0];
		} else {
			return;
		}
		
		//BufferedImage img = this.bs.getPhotoFromFile(id);
		File img = this.bs.getPhotoFromFile(uid, id, type_personne[0]);
		
		
		if (img != null) {
			Sha1[] sha1s = this.bs.saveImage(img);
			Sha1 oldOriginSha1 = this.getOriginSha1ForUid(uid);
			if (oldOriginSha1 != null) {
				if (!oldOriginSha1.equals(sha1s[1])) {
					this.bs.deleteFile(this.getSha1ForUid(uid));
				} else {
					System.out.println("Photo déjà existante pour l'uid "+uid + " (de type "+profil[0]+")");
					return;
				}
				query = "UPDATE sha1 SET sha1='" + sha1s[0].getSha1() + "', originsha1='"+sha1s[1].getSha1()+"' WHERE uid = '" + uid + "'";
				System.out.println("Photo mise à jour pour l'uid " + uid + " (de type "+profil[0] +")");
			} else {
				query = "INSERT INTO sha1(uid, sha1, originsha1) VALUE ('"+uid+"', '"+sha1s[0].getSha1()+"', '"+sha1s[1].getSha1()+"')";	
				System.out.println("Photo enregistrée pour l'uid " + uid + " (de type "+profil[0] +")");
			}
			
			//Connection con = connMgr.getConnection("refphoto");
			this.db.connect();
	        //if (con == null) {
			if (!this.db.isConnected()) {
	        	System.out.println("Can't get connection");
	            return;
	        }
	        //ResultSet rs = null;
	        //ResultSetMetaData md = null;
	        //Statement stmt = null;
	        //try {
	            //stmt = con.createStatement();
	            //stmt.executeUpdate(query);
	        	this.db.query(query);
	            //rs = null;
	            //stmt.close();
	        /*}
	        catch (SQLException e) {
	        	System.out.println("erreur:"+e.getMessage());
	        }*/
	        //connMgr.freeConnection("refphoto", con);
	        this.db.disconnect();
			
			this.mc.delete("sha1_"+uid);
		} else {
			System.out.println("Pas de photo pour l'uid "+ uid + " (de type "+ profil[0]+")");
		}
		
		
	}
	
	public void delete() {
		String query = "(&(objectclass=*)(" + this.ldap.getFieldid() + "=*))";
		
		String[] attributes = { this.ldap.getFieldid() };		
		this.ldap.Openconnection();
		Hashtable<String, String[]> results[] = this.ldap.searchMultiple(query, attributes, "");
		this.ldap.Closeconnection();

		LinkedList<String> uidList = new LinkedList<String>();
		
		if (results.length == 0) {
			return;
		}
		
		String[] ids = null;
		
		for(int i=0; i<results.length;i++) {
			ids = results[i].get(this.ldap.getFieldid());
			//for(int j=0; j<ids.length; j++) {
				//System.out.println("ids[0]=["+ids[0]+"]");
				uidList.add(ids[0]);
			//}
		}
		
		String query2 = "(&(objectclass=*)(" + this.ldap.getFieldstructureid() + "=*))";
		
		String[] attributes2 = { this.ldap.getFieldstructureid() };		
		this.ldap.Openconnection();
		Hashtable<String, String[]> results2[] = this.ldap.searchMultiple(query2, attributes2, "ou=structures");
		this.ldap.Closeconnection();
		
		String[] structureids = null;
		
		for(int i=0; i<results2.length;i++) {
			structureids = results2[i].get(this.ldap.getFieldstructureid());
			//for(int j=0; j<ids.length; j++) {
				//System.out.println("ids[0]=["+ids[0]+"]");
				uidList.add(structureids[0]);
			//}
		}
		
		/*
		Iterator it=uidList.iterator();
		String tmp="";
		while(it.hasNext()) {
			tmp=(String)it.next();
			System.out.println(tmp);
		}
		*/
		
		String query3 = "SELECT uid, sha1 FROM sha1";
		this.db.connect();
		if (!this.db.isConnected()) {
        	System.out.println("Can't get connection");
            return;
        }		
        ResultSet rs = null;
        @SuppressWarnings("unused")
		ResultSet rs2 = null;
        String uid="";
        String sha1="";
        try {
        	rs = this.db.query(query3);
        	while(rs.next()) {        	           
            	uid = rs.getString(1);
            	sha1 = rs.getString(2);
            	
            	System.out.println("-----------------------------");
            	System.out.println("uid = [" + uid + "]");
            	System.out.println("sha1 = [" + sha1 + "]");
            	
            	if (uidList.contains(uid)) {
            		System.out.println("uid trouvé dans le LDAP = [OUI]");
            		System.out.println("photo conservée");
            	} else {
            		System.out.println("uid trouvé dans le LDAP = [NON]");
            		Sha1 s=new Sha1("");
            		s.setSha1(sha1);
            		if (this.bs.deleteFile(s)) {
            			System.out.println("photo supprimée");
            			String query4 = "DELETE FROM sha1 WHERE uid="+uid;
                        rs2 = this.db.query(query4);
                    }
            	}
            	
            }
        }
        catch (SQLException e) {
        	System.out.println("erreur:"+e.getMessage());
        }
        this.db.disconnect();
		
	}
	
	public Sha1 getOriginSha1ForUid(String uid) {
		Sha1 retour = null;
		String valeur = "";
		String query = " SELECT originsha1 FROM sha1 WHERE uid = '" + uid + "' ";
		
		//Connection con = connMgr.getConnection("refphoto");
		this.db.connect();
        //if (con == null) {
		if (!this.db.isConnected()) {
        	System.out.println("Can't get connection");
            return null;
        }
        ResultSet rs = null;
        //ResultSetMetaData md = null;
        //Statement stmt = null;        
        try {
            //stmt = con.createStatement();
            //rs = stmt.executeQuery(query);
        	rs = this.db.query(query);        	
            //md = rs.getMetaData();
            if (rs.next()) {
            	valeur = rs.getString(1);
            	retour = new Sha1("");
                retour.setSha1(valeur);
            } else {
            	//this.db.disconnect();
            	retour = null;
            }
            //stmt.close();
            //rs.close();
        }
        catch (SQLException e) {
        	System.out.println("erreur:"+e.getMessage());
        	retour = null;
        }
        //connMgr.freeConnection("refphoto", con);
        this.db.disconnect();
		return retour;
	}
	
	public void closeMc() {
		this.mc.closeConnection();
	}
}