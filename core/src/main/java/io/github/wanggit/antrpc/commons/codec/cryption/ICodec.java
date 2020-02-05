package io.github.wanggit.antrpc.commons.codec.cryption;

public interface ICodec {

    /**
     * 设置密钥
     *
     * @param key 密钥Key
     */
    void setKey(String key);

    byte[] decrypt(byte[] content);

    byte[] encrypt(byte[] content);
}
