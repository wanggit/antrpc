package io.github.wanggit.antrpc.commons.codec.cryption;

import io.github.wanggit.antrpc.commons.config.CodecConfig;

public class CodecHolder implements ICodecHolder {

    private final ICodec cache;

    public CodecHolder(CodecConfig codecConfig)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (codecConfig.isEnable()) {
            cache = (ICodec) Class.forName(codecConfig.getType()).newInstance();
            cache.setKey(codecConfig.getKey());
        } else {
            cache = new NoOpCodec();
        }
    }

    @Override
    public ICodec getCodec() {
        return cache;
    }
}
