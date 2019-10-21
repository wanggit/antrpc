package antrpc.commons.config;

import antrpc.commons.codec.cryption.AESCodec;
import lombok.Data;

/** 加解密的配置项 */
@Data
public class CodecConfig {

    /** 是否启用 */
    private boolean enable = false;

    /** 加密类型 */
    private String type = AESCodec.class.getName();

    /** 密钥Key */
    private String key;
}
