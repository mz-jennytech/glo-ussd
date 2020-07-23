/*
 * Crown Interactive. Proprietary.
 */
package ussd.etranzact.glo.utils;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import static ussd.etranzact.glo.utils.Utility.getAutoSwitchMessage;

/**
 * Contains encryption and digest utilities.
 *
 * @author oluremi.adekanmbi
 */
public class Crypto {

    private static final Logger L = Logger.getLogger(Crypto.class);

    private static final String UTF_8_ENCODING = "UTF-8";
    private static final String SALT = "SLCR"; // DON'T EVER CHANGE!
    private static final int ROUNDS = 100; // DON'T EVER CHANGE!

    private static final String HASH_SALT = "d8a8e885-ecce-42bb-8332-894f20f0d8ed";
    private static final int HASH_ITERATIONS = 1000;

    private static final int OBSCURATION_FACTOR = 2;

    public static final String DESEDE_ECB_NO_PADDING = "DESede/ECB/Nopadding";
    public static final String DESEDE_ECB_PKCS5_PADDING = "DESede/ECB/PKCS5Padding";

    private static final Random randomGen;
    private static SecureRandom secureRandomGen;
    private static Date secureRandomGenCreationTime;

    static {
        randomGen = new Random();
        createSecureRandom();
    }

    private Crypto() {
    }

    public static String encodeMD5(String data, String salt)
            throws NullPointerException {
        if (data == null) {
            throw new NullPointerException("data to encode cannot be null");
        }
        return DigestUtils.md5Hex(salt + "sselfccaree" + data);//do not change
    }

    public static String encodeMD5NoSalt(String data)
            throws NullPointerException {
        if (data == null) {
            throw new NullPointerException("data to encode cannot be null");
        }
        return DigestUtils.md5Hex(data);
        //return Arrays.toString(md5);
    }

    private static void createSecureRandom() {
        try {
            secureRandomGen = SecureRandom.getInstance("SHA1PRNG");
            secureRandomGenCreationTime = DateTimeUtil.getCurrentDate();
        } catch (NoSuchAlgorithmException ex) {
            L.error("Unable to create SecureRandom instance.", ex);
        }
    }

    /**
     * Digests a password according to our specs.
     */
    public static String digestPassword(String input) throws NoSuchAlgorithmException {
        String saltedInput = SALT + input;
        byte[] bytes = saltedInput.getBytes(Charset.forName(UTF_8_ENCODING));
        for (int i = 1; i <= ROUNDS; i++) {
            bytes = computeSHA256Hash(bytes);
        }
        return toHexString(bytes);
    }

    private static byte[] computeSHA256Hash(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(input);
        return digest.digest();
    }

    public static String hexHashSHA256(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(input);
        return toHexString(digest.digest());
    }

    private static String toHexString(byte[] digest) {
        StringBuilder hashString = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            String hex = Integer.toHexString(digest[i]);
            if (hex.length() == 1) {
                hashString.append('0');
                hashString.append(hex.charAt(hex.length() - 1));
            } else {
                hashString.append(hex.substring(hex.length() - 2));
            }
        }
        return hashString.toString();
    }

    /**
     * Replaces random regions of a String with the underscore character
     * <code>_</code>.
     *
     * @param input original text.
     * @return obscured text.
     */
    public static String obscureRandomRegions(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder sb = new StringBuilder(input);
        if (input.length() < 3) {
            return sb.replace(0, 1, "_").toString();
        }

        int length = input.length();
        boolean[] flag = new boolean[length];
        Arrays.fill(flag, false);
        // Yes I know this is integer division! No problem.
        int obscurationSize = length / OBSCURATION_FACTOR;

        for (int a = 1; a <= obscurationSize; a++) {
            int pick = randomGen.nextInt(length);
            while (flag[pick]) {
                pick = randomGen.nextInt(length);
            }
            sb.replace(pick, pick + 1, "_");
            flag[pick] = true;
        }

        return sb.toString();
    }

    public static String generateTenDigitPin() {
        return generatePin(10);
    }

    public static String generateSixDigitPin() {
        return generatePin(6);
    }

    public static String generateFourDigitPin() {
        return generatePin(4);
    }

    private static String generatePin(int digits) {
        if (digits <= 0) {
            L.warn("Attempt to generate a pin with an invalid number of digits. Digits = " + digits);
            return "";
        }

        if (true) {
            createSecureRandom();
        }

        int max = 0;
        for (int a = 0; a < digits; a++) {
            max += 9 * Math.pow(10, a);
        }

        return String.format("%0" + digits + "d", 1 + secureRandomGen.nextInt(max));
    }

    private static byte[] createKey(String input) {
        byte[] tmp = hexStringToByteArray(input);
        byte[] key = new byte[24];
        System.arraycopy(tmp, 0, key, 0, 16);
        System.arraycopy(tmp, 0, key, 16, 8);
        return key;
    }

    public static byte[] hexStringToByteArray(String hexString) {
        return DatatypeConverter.parseHexBinary(hexString);
    }

    public static String byteArrayToHexString(byte[] array) {
        return DatatypeConverter.printHexBinary(array);
    }

    //////////////////////////////////WebConnect Hashing////////////////////////////////////////////////////////
//   public static String hashPassword(String passwordToHash, String mobileNumber) throws Exception {
//      return hashToken(passwordToHash, mobileNumber + HASH_SALT);
//   }
//   private static String hashToken(String token, String salt) throws Exception {
//      return HashUtil.byteToBase64(getHash(HASH_ITERATIONS, token, salt.getBytes()));
//   }
    public static byte[] getHash(int numberOfIterations, String password, byte[] salt) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.reset();
        digest.update(salt);
        byte[] input = digest.digest(password.getBytes("UTF-8"));
        for (int i = 0; i < numberOfIterations; i++) {
            digest.reset();
            input = digest.digest(input);
        }
        return input;
    }

    public static String getTimeDiff(Date dateOne, Date dateTwo) {
        String diff = "";
        long timeDiff = Math.abs(dateOne.getTime() - dateTwo.getTime());
        diff = String.format("%d", TimeUnit.MILLISECONDS.toHours(timeDiff), TimeUnit.MILLISECONDS.toMinutes(timeDiff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeDiff)));
        return diff;
    }

    public static void main(String[] args) throws ParseException {
        String dg = "0349023890";
        System.out.println(dg.replace(dg.substring(0, 6), "******"));
        //.err.println("Response Message: "+getAutoSwitchMessage(-1));
//         Calendar now = Calendar.getInstance();
//            now.add(Calendar.HOUR, 1);
//            //System.out.println(now.getTime());
//            
//            System.out.println(getTimeDiff(now.getTime(),new Date()));

    }

//   public static void main(String[] args) {
//      String password = "admin@123";
//      String mobileNumber = "08098753155";
//      String saved = "", incoming = "";
//      try {
//         saved = hashPassword(password, mobileNumber);
//         System.out.println(saved);
//      } catch (Exception ex) {
//         java.util.logging.Logger.getLogger(Crypto.class.getName()).log(Level.SEVERE, null, ex);
//      }
//
//      try {
//         incoming = hashPassword(password, mobileNumber);
//         System.out.println(incoming);
//      } catch (Exception ex) {
//         java.util.logging.Logger.getLogger(Crypto.class.getName()).log(Level.SEVERE, null, ex);
//      }
//
//      if (saved.equals(incoming)) {
//         System.out.println("They are equal to each other!");
//      }
//   }
    //////////////////////////////////WebConnect Hashing////////////////////////////////////////////////////////
}
