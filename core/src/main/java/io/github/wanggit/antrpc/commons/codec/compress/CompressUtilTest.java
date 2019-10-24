package io.github.wanggit.antrpc.commons.codec.compress;

import org.junit.Assert;
import org.junit.Test;

public class CompressUtilTest {

    @Test
    public void testCompress() throws Exception {
        String content = "this is a test,sfasdfadfafdadfadfada";
        byte[] origins = content.getBytes();
        System.out.println(origins.length);
        byte[] compress = CompressUtil.compress(origins);
        System.out.println(compress.length);
        byte[] uncompress = CompressUtil.uncompress(compress);
        Assert.assertEquals(content, new String(uncompress));
    }

}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme