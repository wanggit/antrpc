package io.github.wanggit.antrpc.commons.config;

import io.github.wanggit.antrpc.commons.codec.cryption.NoOpCodec;
import lombok.Data;

/** 加解密的配置项 */
@Data
public class CodecConfig {

    /** 是否启用 */
    private boolean enable = false;

    /** 加密类型 */
    private String type = NoOpCodec.class.getName();

    /** 密钥Key */
    private String key;
}
