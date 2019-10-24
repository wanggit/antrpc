package io.github.wanggit.antrpc.commons.codec.cryption;

public class NoOpCodec implements ICodec {
    @Override
    public void setKey(String key) {}

    @Override
    public byte[] decrypt(byte[] content) {
        return content;
    }

    @Override
    public byte[] encrypt(byte[] content) {
        return content;
    }
}
