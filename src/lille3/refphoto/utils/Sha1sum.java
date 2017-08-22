package lille3.refphoto.utils;


import java.io.File;
import java.io.FileInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import org.apache.log4j.Logger;


/**
 * Calcule et compare les empreintes MD5 et SHA-1 des fichiers.
 */
public class Sha1sum {
	/**
     * Le logger.
     */
    private static Logger logger = Logger.getLogger(Sha1sum.class.getName());
    /**
     * compare le sha1sum donné avec l'empreinte du fichier local file.
     * @param file le fichier dont on doit calculer l'empreinte sha1
     * @param sha1sum l'empreinte à comparer
     * @return true si l'empreinte du fichier correspond à sha1sum
     */
    public static boolean compareSha1sum(File file, String sha1sum) {
        boolean res = false;
        if (sha1sum.equals(sha1sum(file))) {
            res = true;
        }
        return res;
    }

    /**
     * Retourne l'empreinte sha1 de fichier file ou null si le
     * fichier n'a pas pu être lu.
     * @param file le fichier dont on doit calculer l'empreinte sha1
     * @return l'empreinte sha1
     */
    public static String sha1sum(File file){
        String localSha1Sum = null;
        if (file.exists() && file.isFile() && file.canRead()){
			try {
				MessageDigest md = MessageDigest.getInstance("SHA-1");
				DigestInputStream dis = new DigestInputStream(new FileInputStream(file), md);
				dis.on(true);
	
				while (dis.read() != -1){
					;
				}
				byte[] b = md.digest();
				localSha1Sum = getHexString(b);
			} catch (Exception e) {
				if (logger.isInfoEnabled())
					logger.info("erreur:"+e.getMessage());
			}
		} else {
			if (logger.isInfoEnabled())
				logger.info("impossible de trouver le fichier : "+file.getAbsolutePath());
		}
        return localSha1Sum;
    }

    /**
     * Compare le md5sum donné avec l'empreinte du fichier local file.
     * L'utilisation de MD5 est déconseillée ! Il existe en effet une faille de sécurité par collision sur MD5
     * Utilisez plutôt SHA-1.
     * @param file le fichier dont on doit calculer l'empreinte md5
     * @param sha1sum l'empreinte à comparer
     * @return true si l'empreinte du fichier correspond à md5sum
     */
    public static boolean compareMd5sum(File file, String md5sum) {
        boolean res = false;
        if (md5sum.equals(md5sum(file))){
            res = true;
        }
        return res;
    }

    /**
     * Retourne l'empreinte md5 de fichier file ou null si le
     * fichier n'a pas pu être lu.
     * @param file le fichier dont on doit calculer l'empreinte md5
     * @return l'empreinte md5
     */
    public static String md5sum(File file) {
        String localMd5Sum = null;
        if (file.exists() && file.isFile() && file.canRead()){
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				DigestInputStream dis = new DigestInputStream(new FileInputStream(file), md);
				dis.on(true);
	
				while (dis.read() != -1){
					;
				}				
				byte[] b = md.digest();
				dis.close();
				localMd5Sum = getHexString(b);
			} catch (Exception e) {
				if (logger.isInfoEnabled())
	    			logger.info("erreur:"+e.getMessage());
			} 
		} else {
			if (logger.isInfoEnabled())
    			logger.info("impossible de trouver le fichier : "+file.getAbsolutePath());
		}
        return localMd5Sum;
    }

	private static String getHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length*2);
		for (byte b : bytes) {
            if (b <= 0x0F && b >= 0x00) { // On rajoute le 0 de poid fort ignoré à la conversion.
                sb.append('0');
            }
			sb.append( String.format("%x", b) );
		}
		return sb.toString();
	}
	
}