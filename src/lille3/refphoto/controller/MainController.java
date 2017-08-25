package lille3.refphoto.controller;
 

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.apache.catalina.connector.Response;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lille3.refphoto.exception.ForbiddenException;
import lille3.refphoto.service.Photoserviceweb;

/*
 * author: Lille3
 * 
 */
 
@Controller
@Scope("request")
public class MainController {
	

	/**
     * Le logger.
     */
    private static Logger logger = Logger.getLogger(MainController.class.getName());
	
    private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB
    
    @Autowired
    private HttpServletRequest request;
    
    @Autowired
    private HttpServletResponse response;
    
    @Autowired
    private ServletContext context;    		
	
    /*@RequestMapping(value = "/test")
    @ResponseBody
    public String test() {
    	if (logger.isInfoEnabled())
			logger.info("call action test() from route /test");
    	
    	Photoserviceweb service = new Photoserviceweb(context);
    	//service.requete();
    	
    	service.importAllPhoto();    	    	
    	
    	return "";
    }*/
    
	@RequestMapping(value = "/token/add/{uid}")
	@ResponseBody
	public String createTokenAction(@PathVariable("uid") String uid) {
		
		if (logger.isInfoEnabled())
			logger.info("call action createTokenAction(uid) from route /token/add");
		
		Photoserviceweb service = new Photoserviceweb(context);
		
		boolean verif_client_valid_server = service.checkValidServer(request);
		
		boolean verif_client_xvalid_server = service.checkXValidServer(request);
	
		if ((verif_client_valid_server) || (verif_client_xvalid_server)) {
			return service.createToken(request, uid);
		}
		
		throw new ForbiddenException();
	}
	
	@RequestMapping(value = "/binary/{uid}")
	public void binaryAction(@PathVariable("uid") String uid) {
		
		if (logger.isInfoEnabled())
			logger.info("call action binaryAction(uid) from route /binary");
		
		Photoserviceweb service = new Photoserviceweb(context);
		
		boolean verif_client_valid_server = service.checkValidServer(request);
		
		boolean verif_client_xvalid_server = service.checkXValidServer(request);

		if ((verif_client_valid_server) || (verif_client_xvalid_server)) {
			String imageUrl = service.getPath(service.createToken(request, uid));		
		
			response.reset();
			response.setBufferSize(DEFAULT_BUFFER_SIZE);
			response.setContentType("image/jpeg");
			
			BufferedInputStream input = null;
	        BufferedOutputStream output = null;
			
			try {			
				input = new BufferedInputStream(new FileInputStream(imageUrl), DEFAULT_BUFFER_SIZE);
			    output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);
						    
	            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
	            int length;
	            while ((length = input.read(buffer)) > -1) {
	                output.write(buffer, 0, length);
	            }
	            output.flush();
			    
	            if (logger.isInfoEnabled())
	    			logger.info("fichier renvoyé: " + imageUrl); 
	            
			} catch (FileNotFoundException e) {
				if (logger.isInfoEnabled())
					logger.info("erreur:"+e.getMessage());
			} catch (IOException e) {
				if (logger.isInfoEnabled())
					logger.info("erreur:"+e.getMessage());
			} finally {            
	            close(output);
	            close(input);
	        }
			return;
		}
		
		throw new ForbiddenException();
	}
	
	@RequestMapping(value = "/tokenEtu/add/{codeetu}")
	@ResponseBody
	public String createTokenEtuAction(@PathVariable("codeetu") String codeetu) {
		
		if (logger.isInfoEnabled())
			logger.info("call action createTokenEtuAction(codeetu) from route /tokenEtu/add");
		
		Photoserviceweb service = new Photoserviceweb(context);
		
		boolean verif_client_valid_server = service.checkValidServer(request);
		
		boolean verif_client_xvalid_server = service.checkXValidServer(request);
	
		if ((verif_client_valid_server) || (verif_client_xvalid_server)) {
			return service.createToken(request, service.getUidByCodEtu(codeetu));
		}
		
		throw new ForbiddenException();
	}
	
	@RequestMapping(value = "/binaryEtu/{codeetu}")
	public void binaryEtuAction(@PathVariable("codeetu") String codeetu) {
		
		if (logger.isInfoEnabled())
			logger.info("call action binaryEtuAction(uid) from route /binaryEtu");
		
		Photoserviceweb service = new Photoserviceweb(context);
		
		boolean verif_client_valid_server = service.checkValidServer(request);
		
		boolean verif_client_xvalid_server = service.checkXValidServer(request);

		if ((verif_client_valid_server) || (verif_client_xvalid_server)) {
			String uid = service.getUidByCodEtu(codeetu);
			String imageUrl = service.getPath(service.createToken(request, uid));		
		
			response.reset();
			response.setBufferSize(DEFAULT_BUFFER_SIZE);
			response.setContentType("image/jpeg");
			
			BufferedInputStream input = null;
	        BufferedOutputStream output = null;
			
			try {			
				input = new BufferedInputStream(new FileInputStream(imageUrl), DEFAULT_BUFFER_SIZE);
			    output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);
						    
	            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
	            int length;
	            while ((length = input.read(buffer)) > -1) {
	                output.write(buffer, 0, length);
	            }
	            output.flush();
			    
	            if (logger.isInfoEnabled())
	    			logger.info("fichier renvoyé: " + imageUrl); 
	            
			} catch (FileNotFoundException e) {
				if (logger.isInfoEnabled())
					logger.info("erreur:"+e.getMessage());
			} catch (IOException e) {
				if (logger.isInfoEnabled())
					logger.info("erreur:"+e.getMessage());
			} finally {            
	            close(output);
	            close(input);
	        }
			return;
		}
		
		throw new ForbiddenException();
	}
	
	@RequestMapping(value = "/tokenPers/add/{codepers}")
	@ResponseBody
	public String createTokenPersAction(@PathVariable("codepers") String codepers) {
		
		if (logger.isInfoEnabled())
			logger.info("call action createTokenPersAction(codepers) from route /tokenPers/add");
		
		Photoserviceweb service = new Photoserviceweb(context);
		
		boolean verif_client_valid_server = service.checkValidServer(request);
		
		boolean verif_client_xvalid_server = service.checkXValidServer(request);
	
		if ((verif_client_valid_server) || (verif_client_xvalid_server)) {
			return service.createToken(request, service.getUidByCodPers(codepers));
		}
		
		throw new ForbiddenException();
	}
	
	@RequestMapping(value = "/binaryPers/{codepers}")
	public void binaryPersAction(@PathVariable("codepers") String codepers) {
		
		if (logger.isInfoEnabled())
			logger.info("call action binaryPersAction(uid) from route /binaryPers");
		
		Photoserviceweb service = new Photoserviceweb(context);
		
		boolean verif_client_valid_server = service.checkValidServer(request);
		
		boolean verif_client_xvalid_server = service.checkXValidServer(request);

		if ((verif_client_valid_server) || (verif_client_xvalid_server)) {
			String uid = service.getUidByCodPers(codepers);
			String imageUrl = service.getPath(service.createToken(request, uid));		
		
			response.reset();
			response.setBufferSize(DEFAULT_BUFFER_SIZE);
			response.setContentType("image/jpeg");
			
			BufferedInputStream input = null;
	        BufferedOutputStream output = null;
			
			try {			
				input = new BufferedInputStream(new FileInputStream(imageUrl), DEFAULT_BUFFER_SIZE);
			    output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);
						    
	            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
	            int length;
	            while ((length = input.read(buffer)) > -1) {
	                output.write(buffer, 0, length);
	            }
	            output.flush();
			    
	            if (logger.isInfoEnabled())
	    			logger.info("fichier renvoyé: " + imageUrl); 
	            
			} catch (FileNotFoundException e) {
				if (logger.isInfoEnabled())
					logger.info("erreur:"+e.getMessage());
			} catch (IOException e) {
				if (logger.isInfoEnabled())
					logger.info("erreur:"+e.getMessage());
			} finally {            
	            close(output);
	            close(input);
	        }
			return;
		}
		
		throw new ForbiddenException();
	}
	
	@RequestMapping(value = "/image/{token}")
	public void imageAction(@PathVariable("token") String token) {
		
		if (logger.isInfoEnabled())
			logger.info("call action imageAction(token) from route /image");
		
		
		Photoserviceweb service = new Photoserviceweb(context);
		
		String imageUrl = service.getPath(token);		
		
		response.reset();
		response.setBufferSize(DEFAULT_BUFFER_SIZE);
		response.setContentType("image/jpeg");
		
		BufferedInputStream input = null;
        BufferedOutputStream output = null;
		
		try {			
			input = new BufferedInputStream(new FileInputStream(imageUrl), DEFAULT_BUFFER_SIZE);
		    output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);
					    
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int length;
            while ((length = input.read(buffer)) > -1) {
                output.write(buffer, 0, length);
            }
            output.flush();
		    
            if (logger.isInfoEnabled())
    			logger.info("fichier renvoyé: " + imageUrl); 
            
		} catch (FileNotFoundException e) {
			if (logger.isInfoEnabled())
				logger.info("erreur:"+e.getMessage());
		} catch (IOException e) {
			if (logger.isInfoEnabled())
				logger.info("erreur:"+e.getMessage());
		} finally {            
            close(output);
            close(input);
        }
		
		
		
	}
	
	private static void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException e) {
            	if (logger.isInfoEnabled())
    				logger.info("erreur:"+e.getMessage());
            }
        }
    }
 
	
}