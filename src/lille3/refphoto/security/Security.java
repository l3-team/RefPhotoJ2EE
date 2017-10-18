package lille3.refphoto.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;


public class Security {
	
	/**
     * Le logger.
     */
    private static Logger logger = Logger.getLogger(Security.class.getName());
    private String active_reverse_proxy;
    private String reverseproxy;
    private String valid_server;
    private String xvalid_server;
    private String xvalid_codeapp;
    
    
    
    public Security(Properties props) {
    	this.active_reverse_proxy = props.getProperty("security.active_reverse_proxy");
    	this.reverseproxy = props.getProperty("security.reverseproxy");
    	this.valid_server = props.getProperty("security.valid_server");
    	this.xvalid_server = props.getProperty("security.xvalid_server");    	
    	this.xvalid_codeapp = props.getProperty("security.xvalid_codeapp");
    }
	
    public boolean checkAddressValidServer(HttpServletRequest request) {
    	boolean check = false;

    	String[] tab_address = this.valid_server.split(",");
    	
    	/*for(int i=0;i<tab_address.length;i++) {
    		logger.info(tab_address[i]);
    	}*/
    	
    	String ip = this.getClientIp(request);    	    
    	    	
    	logger.info("ip checked:" + ip);
    	
    	boolean verif_ip = ArrayUtils.contains(tab_address, ip);
    	
    	if (verif_ip) {
    		logger.info("authorized");
    	} else {
    		logger.info("not authorized");
    	}
    	
    	String host = this.getHostName(ip);
    	
    	logger.info("host check:" + host);
    	
    	boolean verif_host = ArrayUtils.contains(tab_address, host);
    	
    	if (verif_host) {
    		logger.info("authorized");
    	} else {
    		logger.info("not authorized");
    	}
    	
    	if ( (verif_ip) || (verif_host) ) {
    		check = true;
    	} else {
    		check = false;
    	}
    	    	
    	return check;
    }
    
    public boolean checkAddressXValidServer(HttpServletRequest request) {
    	boolean check = false;

    	String[] tab_address = this.xvalid_server.split(",");
    	
    	/*for(int i=0;i<tab_address.length;i++) {
    		logger.info(tab_address[i]);
    	}*/
    	
    	String ip = this.getClientIp(request);
    	    	
    	logger.info("ip checked:" + ip);
    	
    	boolean verif_ip = ArrayUtils.contains(tab_address, ip);
    	
    	if (verif_ip) {
    		logger.info("authorized");
    	} else {
    		logger.info("not authorized");
    	}
    	
    	String host = this.getHostName(ip);
    	
    	logger.info("host check:" + host);
    	
    	boolean verif_host = ArrayUtils.contains(tab_address, host);
    	
    	if (verif_host) {
    		logger.info("authorized");
    	} else {
    		logger.info("not authorized");
    	}
    	
    	if ( (verif_ip) || (verif_host) ) {
    		check = true;
    	} else {
    		check = false;
    	}
    	    	
    	return check;
    }
    
    public boolean checkXValidCodeapp(String code) {
    	boolean check = false;

    	String[] tab_code = this.xvalid_codeapp.split(",");    	
    	    	    	
    	logger.info("code checked:" + code);
    	
    	boolean verif_code = ArrayUtils.contains(tab_code, code);
    	
    	if (verif_code) {
    		logger.info("authorized");
    	} else {
    		logger.info("not authorized");
    	}
    	
    	if (verif_code) {
    		check = true;
    	} else {
    		check = false;
    	}
    	    	
    	return check;
    }
    
	private String getClientIp(HttpServletRequest request) {
		String remoteAddr = "";
		if (this.active_reverse_proxy.equals("true")) {
            if (!request.getHeader("X-FORWARDED-FOR").equals("")) {
            	remoteAddr = request.getHeader("X-FORWARDED-FOR");
            } else if (!request.getHeader("x-forwarded-for").equals("")) {
            	remoteAddr = request.getHeader("x-forwarded-for");
            }
        } else {
            remoteAddr = request.getRemoteAddr();            
        }
        return remoteAddr;		
	}
	
	private String getHostName(String ip) {
		String host="";
		InetAddress inad;
		try {
			inad = InetAddress.getByName(ip);
			host = inad.getHostName();
		} catch (UnknownHostException e) {
			if (logger.isInfoEnabled())
				logger.info("erreur:"+e.getMessage());
		}
		return host;
	}
	
}