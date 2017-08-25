package lille3.refphoto.binarystore;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import lille3.refphoto.utils.Sha1;
import lille3.refphoto.utils.Sha1sum;

public class Binarystore {
	/**
     * Le logger.
     */
    private static Logger logger = Logger.getLogger(Binarystore.class.getName());
	private String photopath = null;
	private String photoread = null;
	private String photodefault = null;
	private String photoblocked = null;
	private String photoresize = null;
	private String photoextension = null;
	
	public Binarystore(Properties props) {
		this.photopath = props.getProperty("photo.path");
		this.photoread = props.getProperty("photo.read");
		this.photodefault = props.getProperty("photo.default");
		this.photoblocked = props.getProperty("photo.blocked");
		this.photoresize = props.getProperty("photo.resize");
		this.photoextension = props.getProperty("photo.extension");		
	}
	
	public String[] buildPathWithSha1(Sha1 sha1) {
		String[] retour = new String[2];
		retour[0] = sha1.getSha1().substring(0, 2) + "/" + sha1.getSha1().substring(2, 4);
		retour[1] = sha1.getSha1().substring(4) + this.photoextension;
		return retour;
	}
	
	public boolean deleteFile(Sha1 sha1) {
		boolean deleted = false;		
		String[] path = this.buildPathWithSha1(sha1);		
		File file = new File(this.photopath + "/" + path[0] + "/" + path[1]);
		if (file.exists()) {
			deleted = file.delete();
		}
		return deleted;
	}
	
	/*public BufferedImage getPhotoFromFile(String id) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(this.photoread + "/" + id + this.photoextension));
		} catch(IOException e) {				
			if (logger.isInfoEnabled())
				logger.info("erreur:"+e.getMessage());
		}
		return image;
	}*/
	
	public File getPhotoFromFile(String uid, String id, String type) {
		int return_val = 0;
		//BufferedImage image = null;
		File image = null;
		//try {
			//image = ImageIO.read(new File(this.photoread + "/" + id + this.photoextension));
		image = new File(this.photoread + "/" + id + this.photoextension);
		
		if (image.exists()) {
			return_val = 0;
		} else {
			image = new File(this.photodefault);
			//image = null;
			return_val = 1;
		}
		
		//} catch(IOException e) {				
		//	if (logger.isInfoEnabled())
		//		logger.info("erreur:"+e.getMessage());
		//}
		
		if (return_val == 0) {
			return image;
		} else {
			System.out.println("Erreur : Fichier " + this.photoread + "/" + id + this.photoextension + " introuvable pour l'uid " + uid + " (de type " + type + "). Utilisation du fichier " + this.photodefault);
		}
		
		return image;
	}
	
	public Sha1[] saveImage(File image) {
	//public Sha1[] saveImage(BufferedImage imageContent) {
		/*ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(imageContent, "jpg", baos);
		} catch (IOException e) {
			if (logger.isInfoEnabled())
				logger.info("erreur:"+e.getMessage());
		}
		try {
			baos.flush();
		} catch (IOException e) {
			if (logger.isInfoEnabled())
				logger.info("erreur:"+e.getMessage());
		}
		byte[] imageInByte = baos.toByteArray();
		String contenu = "";
		try {
			contenu = new String(imageInByte, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			if (logger.isInfoEnabled())
				logger.info("erreur:"+e.getMessage());
		}
		Sha1 imageOriginSha1 = new Sha1(contenu);
		Sha1 imageSha1 = null;
		try {
			baos.close();
		} catch (IOException e) {
			if (logger.isInfoEnabled())
				logger.info("erreur:"+e.getMessage());
		}*/
		
		/*String fname = "";
		try {
			fname = "/tmp/" + UUID.randomUUID().toString() + ".jpg";
			System.out.println("fname=["+fname+"]");
			ImageIO.write(imageContent, "jpg", new File(fname));
		} catch (IOException e) {
			if (logger.isInfoEnabled())
				logger.info("erreur:"+e.getMessage());
			return null;
		}*/
		
		//String alea = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(13).toLowerCase();
		//System.out.println("alea"+alea);
		
		BufferedImage imageContent = null;
		try {
			imageContent = ImageIO.read(image);    			    		
		} catch(IOException e) {				
			if (logger.isInfoEnabled())
				logger.info("erreur:"+e.getMessage());
		}
		
		Sha1 imageOriginSha1 = new Sha1("");
		Sha1 imageSha1 = null;
		//imageOriginSha1.setSha1(Sha1sum.sha1sum(new File(fname)));
		imageOriginSha1.setSha1(Sha1sum.sha1sum(image));
		//System.out.println("sha1sum=["+Sha1sum.sha1sum(image)+"]");
		
		//File ff = new File(fname);
		//ff.delete();
		
		
        if (!this.photoresize.equals("")) {        	        
        	int type = imageContent.getType() == 0? BufferedImage.TYPE_INT_ARGB : imageContent.getType();
        	String[] values = this.photoresize.split("x");
        	int height = Integer.parseInt(values[1]);
        	int width = Integer.parseInt(values[0]);
        	imageContent = this.resizeImage(imageContent, type, height, width);
        	/*baos = new ByteArrayOutputStream();
        	try {
				ImageIO.write(imageContent, "jpg", baos);
			} catch (IOException e) {
				if (logger.isInfoEnabled())
					logger.info("erreur:"+e.getMessage());
			}
        	try {
				baos.flush();
			} catch (IOException e) {
				if (logger.isInfoEnabled())
					logger.info("erreur:"+e.getMessage());
			}
        	imageInByte = baos.toByteArray();
        	try {
				contenu = new String(imageInByte, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				if (logger.isInfoEnabled())
					logger.info("erreur:"+e.getMessage());
			}
        	imageSha1 = new Sha1(contenu);
        	try {
				baos.close();
			} catch (IOException e) {
				if (logger.isInfoEnabled())
					logger.info("erreur:"+e.getMessage());
			}*/
        	
        	
        	String fname_resized = "";
    		try {
    			fname_resized = "/tmp/" + UUID.randomUUID().toString() + ".jpg";
    			//System.out.println("fname_resized=["+fname_resized+"]");
    			ImageIO.write(imageContent, "jpg", new File(fname_resized));
    		} catch (IOException e) {
    			if (logger.isInfoEnabled())
    				logger.info("erreur:"+e.getMessage());
    			return null;
    		}
        	
    		imageSha1 = new Sha1("");;
    		imageSha1.setSha1(Sha1sum.sha1sum(new File(fname_resized)));
    		
			File fff = new File(fname_resized);
			fff.delete();
    		
    		
        	
        } else {
        	imageSha1 = imageOriginSha1;
        }

        String[] path = this.buildPathWithSha1(imageSha1);
        File f = new File(this.photopath + "/" + path[0] + "/" + path[1]);
        File dir = new File(this.photopath + "/" + path[0]);
        if(!dir.exists()) {
            dir.mkdirs();
        }

        if(!f.exists()) {
        	try {
				ImageIO.write(imageContent, "jpg", f);
			} catch (IOException e) {
				if (logger.isInfoEnabled())
					logger.info("erreur:"+e.getMessage());
			}
        }

        return new Sha1[] { imageSha1, imageOriginSha1 };
    }
	
	private BufferedImage resizeImage(BufferedImage originalImage, int type, int height, int width){
		
		
		BufferedImage resizedImage = new BufferedImage(width, height, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();
		
		/*g.setComposite(AlphaComposite.Src);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);*/
		
		return resizedImage;
	}
	
	public String getPathPhotoDefault() {
		return this.photodefault;
	}
	
	public String getPathPhotoBlocked() {
		return this.photoblocked;
	}
	
	public String getPhotoPath() {
		return this.photopath;
	}
	
	public String getPhotoRead() {
		return this.photoread;
	}
	
	public String getPhotoResize() {
		return this.photoresize;
	}
	
	public String getPhotoExtension() {
		return this.photoextension;
	}
}