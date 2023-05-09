package client.utils;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHasher {
    /**
     * Hashes password;.
     *
     * @param password Password itself.
     * @return Hashed password.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD2");
            byte[] bytes = md.digest(password.getBytes());
            BigInteger integers = new BigInteger(1, bytes);
            String newPassword = integers.toString(16);
            while (newPassword.length() < 32) {
                newPassword = "0" + newPassword;
            }
            return newPassword;
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
