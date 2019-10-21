package antrpc.commons.codec.cryption;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/** AES加解密 */
@Slf4j
public class AESCodec implements ICodec {

    private static final String KEY_ALGORITHM = "AES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding"; // 默认的加密算法

    private String key;

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public byte[] decrypt(byte[] content) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(key));
            return cipher.doFinal(content);
        } catch (Exception ex) {
            if (log.isErrorEnabled()) {
                log.error("decrypt error. ", ex);
            }
        }
        return null;
    }

    @Override
    public byte[] encrypt(byte[] content) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(key));
            return cipher.doFinal(content);
        } catch (Exception ex) {
            if (log.isErrorEnabled()) {
                log.error("encrypt error. ", ex);
            }
        }
        return null;
    }

    private static SecretKeySpec getSecretKey(String key) {
        KeyGenerator kg = null;
        try {
            kg = KeyGenerator.getInstance(KEY_ALGORITHM);
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(key.getBytes());
            kg.init(128, random);
            SecretKey secretKey = kg.generateKey();
            return new SecretKeySpec(secretKey.getEncoded(), KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException ex) {
            if (log.isErrorEnabled()) {
                log.error("getSecretKey error. ", ex);
            }
        }
        return null;
    }
}
