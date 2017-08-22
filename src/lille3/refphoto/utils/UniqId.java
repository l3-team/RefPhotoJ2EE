package lille3.refphoto.utils;

import java.io.IOException; 
import java.net.InetAddress; 
import java.security.MessageDigest; 
import java.security.NoSuchAlgorithmException; 
import java.security.SecureRandom; 
import java.util.Random; 
import java.util.concurrent.atomic.AtomicLong; 
import java.util.concurrent.locks.ReentrantLock; 
 
import org.apache.log4j.Logger; 
 
public class UniqId { 
	/**
     * Le logger.
     */
    private static Logger logger = Logger.getLogger(UniqId.class.getName());
    private static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' }; 
    private static UniqId me = new UniqId(); 
    private String hostAddr; 
    private Random random = new SecureRandom(); 
    private MessageDigest mHasher; 
    private UniqTimer timer = new UniqTimer(); 
 
    private ReentrantLock opLock = new ReentrantLock(); 
 
    private UniqId() { 
        try { 
            InetAddress addr = InetAddress.getLocalHost(); 
 
            hostAddr = addr.getHostAddress(); 
        } catch (IOException e) { 
            logger.error("[UniqID] Get HostAddr Error", e); 
            hostAddr = String.valueOf(System.currentTimeMillis()); 
        } 
 
        if (null == hostAddr || hostAddr.trim().length() == 0 || "127.0.0.1".equals(hostAddr)) { 
            hostAddr = String.valueOf(System.currentTimeMillis()); 
        } 
 
        if (logger.isDebugEnabled()) { 
            logger.debug("[UniqID]hostAddr is:" + hostAddr); 
        } 
 
        try { 
            mHasher = MessageDigest.getInstance("MD5"); 
        } catch (NoSuchAlgorithmException nex) { 
            mHasher = null; 
            logger.error("[UniqID]new MD5 Hasher error", nex); 
        } 
    } 
 
    public static UniqId getInstance() { 
        return me; 
    } 
 
    public long getUniqTime() { 
        return timer.getCurrentTime(); 
    } 
 
    public String getUniqID() { 
        StringBuffer sb = new StringBuffer(); 
        long t = timer.getCurrentTime(); 
 
        sb.append(t); 
 
        sb.append("-"); 
 
        sb.append(random.nextInt(8999) + 1000); 
 
        sb.append("-"); 
        sb.append(hostAddr); 
 
        sb.append("-"); 
        sb.append(Thread.currentThread().hashCode()); 
 
        if (logger.isDebugEnabled()) { 
            logger.debug("[UniqID.getUniqID]" + sb.toString()); 
        } 
 
        return sb.toString(); 
    } 
 
    public String getUniqIDHashString() { 
        return hashString(getUniqID()); 
    } 
 
    public byte[] getUniqIDHash() { 
        return hash(getUniqID()); 
    } 
 
    public byte[] hash(String str) { 
        opLock.lock(); 
        try { 
            byte[] bt = mHasher.digest(str.getBytes()); 
            if (null == bt || bt.length != 16) { 
                throw new IllegalArgumentException("md5 need"); 
            } 
            return bt; 
        } finally { 
            opLock.unlock(); 
        } 
    } 
 
    public String hashString(String str) { 
        byte[] bt = hash(str); 
        return bytes2string(bt); 
    } 
 
    public String bytes2string(byte[] bt) { 
        int l = bt.length; 
 
        char[] out = new char[l << 1]; 
 
        for (int i = 0, j = 0; i < l; i++) { 
            out[j++] = digits[(0xF0 & bt[i]) >>> 4]; 
            out[j++] = digits[0x0F & bt[i]]; 
        } 
 
        if (logger.isDebugEnabled()) { 
            logger.debug("[UniqID.hash]" + (new String(out))); 
        } 
 
        return new String(out); 
    } 
 
    private class UniqTimer { 
        private AtomicLong lastTime = new AtomicLong(System.currentTimeMillis()); 
 
        public long getCurrentTime() { 
            return this.lastTime.incrementAndGet(); 
        } 
    } 
}