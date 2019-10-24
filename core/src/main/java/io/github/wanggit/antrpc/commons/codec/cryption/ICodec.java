package io.github.wanggit.antrpc.commons.codec.cryption;

public interface ICodec {

    /**
     * 设置密钥
     *
     * @param key
     */
    void setKey(String key);

    /**
     * 解密
     *
     * @param content
     * @return
     */
    byte[] decrypt(byte[] content);

    /**
     * 加密码
     *
     * @param content
     * @return
     */
    byte[] encrypt(byte[] content);
}
