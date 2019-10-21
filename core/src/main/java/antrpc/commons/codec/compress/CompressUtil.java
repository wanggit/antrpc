package antrpc.commons.codec.compress;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public class CompressUtil {

    public static byte[] compress(byte[] data) {
        ByteArrayOutputStream byteArrayOutputStream = null;
        CompressorOutputStream cos = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            cos =
                    new CompressorStreamFactory()
                            .createCompressorOutputStream(
                                    CompressorStreamFactory.BZIP2, byteArrayOutputStream);
            IOUtils.copy(new ByteArrayInputStream(data), cos);
            cos.close();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("An exception occurred while compressing the data.", e);
            }
            throw new CompressException(e);
        } finally {
            if (null != byteArrayOutputStream) {
                try {
                    byteArrayOutputStream.flush();
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != cos) {
                try {
                    cos.flush();
                    cos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static byte[] uncompress(byte[] data) {
        ByteArrayOutputStream byteArrayOutputStream = null;
        CompressorInputStream in = null;
        try {
            in =
                    new CompressorStreamFactory()
                            .createCompressorInputStream(
                                    CompressorStreamFactory.BZIP2, new ByteArrayInputStream(data));
            byteArrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copy(in, byteArrayOutputStream);
            in.close();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("An exception occurred while decompressing the data.", e);
            }
            throw new CompressException(e);
        } finally {
            if (null != byteArrayOutputStream) {
                try {
                    byteArrayOutputStream.flush();
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
