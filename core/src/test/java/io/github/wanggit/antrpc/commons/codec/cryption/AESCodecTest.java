package io.github.wanggit.antrpc.commons.codec.cryption;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class AESCodecTest {

    @Test
    public void testBigDataCodec() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            builder.append(RandomStringUtils.randomAlphanumeric(50));
        }
        internalDoCodec(builder.toString());
    }

    @Test
    public void testCodec() {
        String content = "hello codec";
        internalDoCodec(content);
    }

    private void internalDoCodec(String content) {
        ICodec codec = new AESCodec();
        byte[] bytes = content.getBytes();
        String key = UUID.randomUUID().toString();
        codec.setKey(key);
        byte[] encrypted = codec.encrypt(bytes);
        byte[] decrypted = codec.decrypt(encrypted);
        String result = new String(decrypted);
        Assert.assertEquals(result, content);
    }
}
