package io.github.wanggit.antrpc.commons.codec.cryption;

import io.github.wanggit.antrpc.commons.config.CodecConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class CodecHolderTest {

    @Test
    public void testCodeHolder()
            throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        String key = UUID.randomUUID().toString();
        CodecConfig config = new CodecConfig();
        config.setEnable(true);
        config.setKey(key);
        ICodecHolder codecHolder = new CodecHolder(config);
        Assert.assertTrue(codecHolder.getCodec() instanceof NoOpCodec);

        config = new CodecConfig();
        config.setKey(key);
        config.setEnable(true);
        config.setType(AESCodec.class.getName());
        codecHolder = new CodecHolder(config);
        Assert.assertTrue(codecHolder.getCodec() instanceof AESCodec);

        config = new CodecConfig();
        config.setKey(key);
        config.setEnable(true);
        config.setType("type");
        try {
            codecHolder = new CodecHolder(config);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ClassNotFoundException);
        }

        config = new CodecConfig();
        config.setKey(key);
        config.setEnable(true);
        config.setType(CodecHolderTest.class.getName());
        try {
            codecHolder = new CodecHolder(config);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ClassCastException);
        }
    }
}
