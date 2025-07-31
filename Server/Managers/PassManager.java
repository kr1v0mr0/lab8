package lab5.Server.Managers;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

public class PassManager {
    private MessageDigest md;
    private String pepper;

    public PassManager(String pepper) {
        try {
            md = MessageDigest.getInstance("MD2");  // Изменено на MD2
            this.pepper = pepper;
        } catch (NoSuchAlgorithmException e) {
            md = null;
        }
    }

    public String getPepper() {
        return pepper;
    }

    public void setPepper(String pepper) {
        this.pepper = pepper;
    }

    public byte[] hashPassword(String salt, String password) {
        try {
            md.reset();
            return md.digest((password + pepper + salt).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public boolean checkPassword(String password, String salt, byte[] hash) {
        try {
            md.reset();
            byte[] newHash = md.digest((password + pepper + salt).getBytes("UTF-8"));
            return Arrays.equals(hash, newHash);
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    public String getRandomString(int len) {
        int leftLimit = 48; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = len;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return generatedString;
    }
}