package scalable.com.shared.classes;
import org.mindrot.jbcrypt.BCrypt;
public class ByCryptHelper {


    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }

    public static boolean verifyHash(String password, String hash) {
        try {
            return BCrypt.checkpw(password, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }

    }
}
