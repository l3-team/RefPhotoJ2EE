package lille3.refphoto.utils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import org.apache.log4j.Logger;

public class Sha1 implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6265640106533803364L;
	/**
     * Le logger.
     */
    private static Logger logger = Logger.getLogger(Sha1.class.getName());
	private String originChain = null;
	private String sha1Chain = null;
	
	public Sha1(String chain) {
		this.originChain = chain;
		this.sha1Chain = this.encryptSha1(this.originChain);
	}
	
	private String encryptSha1(String chain) {
	    String sha1 = "";
	    try {
	        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
	        crypt.reset();
	        crypt.update(chain.getBytes("UTF-8"));
	        sha1 = byteToHex(crypt.digest());
	    } catch(NoSuchAlgorithmException e) {
	    	if (logger.isInfoEnabled())
    			logger.info("erreur:"+e.getMessage());
	    }
	    catch(UnsupportedEncodingException e) {
	    	if (logger.isInfoEnabled())
    			logger.info("erreur:"+e.getMessage());
	    }
	    return sha1;
	}

	private static String byteToHex(final byte[] hash) {
	    Formatter formatter = new Formatter();
	    for (byte b : hash) {
	        formatter.format("%02x", b);
	    }
	    String result = formatter.toString();
	    formatter.close();
	    return result;
	}
	
	public void setSha1(String sha1) {
		this.sha1Chain = sha1;
	}
	
	public void setOriginChain(String originChain) {
		this.originChain = originChain;
	}
	
	public String getSha1() {
		return this.sha1Chain;
	}
	
	public String getOriginchain() {
		return this.originChain;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sha1 other = (Sha1) obj;
		//if (!this.getOriginchain().equals(other.getOriginchain()))
			//return false;
		if (!this.getSha1().equals(other.getSha1()))
			return false;
		return true;
  }
}