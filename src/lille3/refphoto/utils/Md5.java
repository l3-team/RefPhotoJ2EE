package lille3.refphoto.utils;

import java.security.MessageDigest;

import org.apache.log4j.Logger;

public class Md5 {
	/**
     * Le logger.
     */
    private static Logger logger = Logger.getLogger(Md5.class.getName());
    private byte[] encodedPassword;
    private String encryptedPassword;
    private String password;
    
    public Md5(String pass) {
                        
        this.setPassword(pass);
        
        byte[] b = this.getEncoded();
               
        StringBuffer sb = new StringBuffer(2*b.length);
        
        for(int i=0; i<b.length; ++i) {
                int k = b[i] & 0xFF;
                if (k < 0x10) {
                        sb.append('0');
                }
                sb.append(Integer.toHexString(k));
        }
        encryptedPassword = new String(sb);
    }
    
    private void setPassword(String clair) {
    	password = clair;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            encodedPassword = messageDigest.digest(clair.getBytes());
        } catch (Exception e) {
        	if (logger.isInfoEnabled())
    			logger.info("erreur:"+e.getMessage());
        }
    }
    
    private byte[] getEncoded() {
        return this.encodedPassword;
    }
    
    public String getMd5() {
        return encryptedPassword;
    }
    
    public String getOriginchain() {
    	return password;
    }
}