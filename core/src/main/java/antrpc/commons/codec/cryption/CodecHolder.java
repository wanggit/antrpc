package antrpc.commons.codec.cryption;

import antrpc.commons.config.CodecConfig;

public class CodecHolder implements ICodecHolder {

    private ICodec cache = null;

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
