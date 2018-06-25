package lille3.refphoto.controller;
 

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.apache.catalina.connector.Response;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
			return service.createToken(request, uid, null);
		}
		
		throw new ForbiddenException();
	}
	
	@RequestMapping(value = "/token/{codeapp}/{uid}")
	@ResponseBody
	public String createTokenActionWithCode(@PathVariable("codeapp") String codeapp, @PathVariable("uid") String uid) {
		
		if (logger.isInfoEnabled())
			logger.info("call action createTokenActionWithCode(uid) from route /token");
		
		Photoserviceweb service = new Photoserviceweb(context);
		
		boolean verif_client_valid_server = service.checkValidServer(request);
		
		boolean verif_client_xvalid_server = service.checkXValidServer(request);
	
		if ((verif_client_valid_server) || (verif_client_xvalid_server)) {
			return service.createToken(request, uid, codeapp);
		}
		
		throw new ForbiddenException();
	}
	
	@RequestMapping(value = "/token/add", method = RequestMethod.POST)
	@ResponseBody
	public String createMultiTokensAction() {
		
		if (logger.isInfoEnabled())
			logger.info("call action createMultiTokens(uid) from route /token/add");
		
		Photoserviceweb service = new Photoserviceweb(context);
		
		boolean verif_client_valid_server = service.checkValidServer(request);
		
		boolean verif_client_xvalid_server = service.checkXValidServer(request);
	
		if ((verif_client_valid_server) || (verif_client_xvalid_server)) {
			
			String body = "";
			try {
				body = getBody(request);
			} catch(IOException e) {
				if (logger.isInfoEnabled())
					logger.info("erreur:"+e.getMessage());
			}
			
			JSONParser parser = new JSONParser();
			
			JSONArray tokens = new JSONArray();
			
			
			
			try {
				
				Object obj = parser.parse(body);
				JSONArray array = (JSONArray)obj;
				
				Iterator it = array.iterator();
				
				String tmp = "";
				while(it.hasNext()) {
					
					String uid = (String) it.next();
					
					
					tokens.add(service.createToken(request, uid, null));
					
				}
				
			} catch(ParseException e) {
				if (logger.isInfoEnabled())
					logger.info("erreur:"+e.getMessage());
			}
			
			StringWriter out = new StringWriter();
			
			try {
				tokens.writeJSONString(out);
			} catch (IOException e) {
				if (logger.isInfoEnabled())
					logger.info("erreur:"+e.getMessage());
			}
			
			return out.toString();
			
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
			String imageUrl = service.getPath(service.createToken(request, uid, null));		
		
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
	
	@RequestMapping(value = "/binaryDownload/{uid}")
	public void binaryDownloadAction(@PathVariable("uid") String uid) {
		
		if (logger.isInfoEnabled())
			logger.info("call action binaryAction(uid) from route /binary");
		
		Photoserviceweb service = new Photoserviceweb(context);
		
		boolean verif_client_valid_server = service.checkValidServer(request);
		
		boolean verif_client_xvalid_server = service.checkXValidServer(request);

		if ((verif_client_valid_server) || (verif_client_xvalid_server)) {
			String imageUrl = service.getPathWithoutVerif(service.createToken(request, uid, null));		
		
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
			return service.createToken(request, service.getUidByCodEtu(codeetu), null);
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
			String imageUrl = service.getPath(service.createToken(request, uid, null));		
		
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
			return service.createToken(request, service.getUidByCodPers(codepers), null);
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
			String imageUrl = service.getPath(service.createToken(request, uid, null));		
		
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
		//logger.info("imageUrl="+imageUrl);
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
	
	/*
	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
	public void authenticateAction() {
		
	}
	*/
	
	@RequestMapping(value = "/upload/{uid}", method = RequestMethod.POST)
	@ResponseBody
	public String uploadAction(@PathVariable("uid") String uid) {
		
		if (logger.isInfoEnabled())
			logger.info("call action uploadAction(uid) from route /upload");
		
		Photoserviceweb service = new Photoserviceweb(context);
		
		return service.uploadPhoto(request, uid);
	}
	
	@RequestMapping(value = "/download/{token}")
	public void downloadAction(@PathVariable("token") String token) {
		
		if (logger.isInfoEnabled())
			logger.info("call action downloadAction(token) from route /download");
		
		
		Photoserviceweb service = new Photoserviceweb(context);
		
		String imageUrl = service.getPathWithoutVerif(token);		
		
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
 
	

	public static String getBody(HttpServletRequest request) throws IOException {

	    String body = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    BufferedReader bufferedReader = null;

	    try {
	        InputStream inputStream = request.getInputStream();
	        if (inputStream != null) {
	            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	            char[] charBuffer = new char[128];
	            int bytesRead = -1;
	            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
	                stringBuilder.append(charBuffer, 0, bytesRead);
	            }
	        } else {
	            stringBuilder.append("");
	        }
	    } catch (IOException ex) {
	        throw ex;
	    } finally {
	        if (bufferedReader != null) {
	            try {
	                bufferedReader.close();
	            } catch (IOException ex) {
	                throw ex;
	            }
	        }
	    }

	    body = stringBuilder.toString();
	    return body;
	}
	
}