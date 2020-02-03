package io.github.wanggit.antrpc.commons.codec;

import io.github.wanggit.antrpc.commons.bean.RpcProtocol;
import io.github.wanggit.antrpc.commons.codec.compress.CompressUtil;
import io.github.wanggit.antrpc.commons.codec.cryption.ICodec;
import io.github.wanggit.antrpc.commons.config.CodecConfig;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcProtocolEncoder extends MessageToByteEncoder<RpcProtocol> {

    private CodecConfig codecConfig;
    private ICodec codec;

    public RpcProtocolEncoder(CodecConfig codecConfig, ICodec codec) {
        this.codecConfig = codecConfig;
        this.codec = codec;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol msg, ByteBuf out) {
        if (msg.getType() == ConstantValues.HB_TYPE) {
            out.writeInt(msg.getCmdId());
            out.writeByte(msg.getType());
            out.writeByte(ConstantValues.UNCODCED);
            out.writeByte(ConstantValues.UNCOMPRESSED);
            out.writeInt(msg.getData().length);
            out.writeBytes(msg.getData());
        } else {
            boolean codecEnabled = null != codecConfig && codecConfig.isEnable();
            if (codecEnabled) {
                msg.setCodec(ConstantValues.CODECED);
                msg.setData(codec.encrypt(msg.getData()));
            } else {
                msg.setCodec(ConstantValues.UNCODCED);
            }
            if (msg.getData().length > ConstantValues.NEED_COMPRESS_LENGTH) {
                if (log.isInfoEnabled()) {
                    log.info(
                            "cmdId = "
                                    + msg.getCmdId()
                                    + ". Packets sent over "
                                    + ConstantValues.NEED_COMPRESS_LENGTH
                                    + " bytes will be compressed before transmission.");
                }
                msg.setZip(ConstantValues.COMPRESSED);
                msg.setData(CompressUtil.compress(msg.getData()));
            } else {
                msg.setZip(ConstantValues.UNCOMPRESSED);
            }
            out.writeInt(msg.getCmdId());
            out.writeByte(msg.getType());
            out.writeByte(msg.getCodec());
            out.writeByte(msg.getZip());
            out.writeInt(msg.getData().length);
            out.writeBytes(msg.getData());
        }
    }
}
