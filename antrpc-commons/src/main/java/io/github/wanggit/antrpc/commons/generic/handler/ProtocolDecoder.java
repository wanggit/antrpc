package io.github.wanggit.antrpc.commons.generic.handler;

import io.github.wanggit.antrpc.commons.bean.RpcProtocol;
import io.github.wanggit.antrpc.commons.codec.compress.CompressUtil;
import io.github.wanggit.antrpc.commons.codec.cryption.ICodec;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProtocolDecoder extends LengthFieldBasedFrameDecoder {

    private ICodec codec;

    public ProtocolDecoder(ICodec codec) {
        super(
                ConstantValues.DECODER_MAX_FRAME_LENGTH,
                ConstantValues.DECODER_LENGTH_FIELD_OFFSET,
                ConstantValues.DECODER_LENGTH_FIELD_LENGTH,
                ConstantValues.DECODER_LENGTH_ADJUSTMENT,
                ConstantValues.DECODER_INITIAL_BYTES_TO_STRIP);
        this.codec = codec;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        if (null == byteBuf) {
            return null;
        }
        if (byteBuf.readableBytes() <= ConstantValues.DECODER_HEADER_SIZE) {
            return null;
        }
        byteBuf.markReaderIndex();
        int cmdId = byteBuf.readInt();
        byte type = byteBuf.readByte();
        byte hasCodec = byteBuf.readByte();
        byte hasZip = byteBuf.readByte();
        byte serialzer = byteBuf.readByte();
        byteBuf.readBytes(8);
        int dataLength = byteBuf.readInt();
        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return null;
        }
        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);
        if (type == ConstantValues.HB_TYPE) {
            if (log.isErrorEnabled()) {
                log.error("Surprise, received should not receive heartbeat package.");
            }
            return null;
        }
        if (hasZip == ConstantValues.COMPRESSED) {
            data = CompressUtil.uncompress(data);
        }
        if (hasCodec == ConstantValues.CODECED) {
            data = codec.decrypt(data);
        }
        RpcProtocol protocol = new RpcProtocol();
        protocol.setCmdId(cmdId);
        protocol.setZip(hasZip);
        protocol.setCodec(hasCodec);
        protocol.setType(type);
        protocol.setSerializer(serialzer);
        protocol.setData(data);
        return protocol;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (log.isErrorEnabled()) {
            log.error("has error.", cause);
        }
        ctx.close();
    }
}
