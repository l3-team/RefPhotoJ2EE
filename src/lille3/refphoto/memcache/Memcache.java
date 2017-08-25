package lille3.refphoto.memcache;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CancellationException;

import org.apache.log4j.Logger;

import lille3.refphoto.utils.Sha1;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;


public class Memcache {
	/**
     * Le logger.
     */
    private static Logger logger = Logger.getLogger(Memcache.class.getName());
    private String host = null;
    private int port = 0;
    private MemcachedClient mc = null;
	
	public Memcache(Properties props) {
		this.host = props.getProperty("memcache.host");
		this.port = Integer.parseInt(props.getProperty("memcache.port"));
		this.initConnection();
	}
	
	private void initConnection() {		
		try {
			mc = new MemcachedClient(AddrUtil.getAddresses(host+":"+port));
		} catch (IOException e) {
			if(logger.isInfoEnabled())
				logger.info("erreur: "+e.getMessage());
		}
	}
	
	public void setTab(String name, int exp, String[] value) {
		try {
			mc.set(name, exp, value);
		} catch(CancellationException e) {
			if(logger.isInfoEnabled())
				logger.info("erreur: "+e.getMessage());
			this.initConnection();
			mc.set(name, exp, value);
		}			
	}
	
	public void setString(String name, int exp, String value) {
		try {
			mc.set(name, exp, value);
		} catch(CancellationException e) {
			if(logger.isInfoEnabled())
				logger.info("erreur: "+e.getMessage());
			this.initConnection();
			mc.set(name, exp, value);
		}
	}
	
	public void setSha1(String name, int exp, Sha1 sha1) {
		try {
			mc.set(name, exp, sha1);
		} catch(CancellationException e) {
			if(logger.isInfoEnabled())
				logger.info("erreur: "+e.getMessage());
			this.initConnection();
			mc.set(name, exp, sha1);
		}
	}
	
	public String[] getTab(String name) {
		String[] tab;
		try {
			tab = (String[]) mc.get(name);
		} catch(CancellationException e) {
			if(logger.isInfoEnabled())
				logger.info("erreur: "+e.getMessage());
			this.initConnection();
			tab = (String[]) mc.get(name);
		}		
		return tab;
	}
	
	public Sha1 getSha1(String name) {
		Sha1 sha1;
		try {
			sha1 = (Sha1) mc.get(name);
		} catch(CancellationException e) {
			if(logger.isInfoEnabled())
				logger.info("erreur: "+e.getMessage());
			this.initConnection();
			sha1 = (Sha1) mc.get(name);
		}
		return sha1;
	}
	
	public String getString(String name) {
		String chaine;
		try {
			chaine = (String) mc.get(name);
		} catch(CancellationException e) {
			if(logger.isInfoEnabled())
				logger.info("erreur: "+e.getMessage());
			this.initConnection();
			chaine = (String) mc.get(name);
		}		
		return chaine;
	}
	
	public void delete(String name) {
		try {
			mc.delete(name);
		} catch(CancellationException e) {
			if(logger.isInfoEnabled())
				logger.info("erreur: "+e.getMessage());
			this.initConnection();
			mc.delete(name);
		}
	}
	
	@Override
    public void finalize() {
    	this.closeConnection();
    }
	
	public void closeConnection() {
		try {
			mc.shutdown();
		} catch(CancellationException e) {
			if(logger.isInfoEnabled())
				logger.info("erreur: "+e.getMessage());
			this.initConnection();
			mc.shutdown();
		}
	}
}
