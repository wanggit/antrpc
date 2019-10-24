package io.github.wanggit.antrpc.commons.codec;

import io.github.wanggit.antrpc.commons.bean.RpcProtocol;
import io.github.wanggit.antrpc.commons.codec.compress.CompressUtil;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcProtocolDecoder extends LengthFieldBasedFrameDecoder {

    private static Integer HEADER_SIZE = 8;
    private static Integer MAX_FRAME_LENGTH = 1024 * 1024;
    private static Integer LENGTH_FIELD_LENGTH = 4;
    private static Integer LENGTH_FIELD_OFFSET = 4;
    private static Integer LENGTH_ADJUSTMENT = 0;
    private static Integer INITIAL_BYTES_TO_STRIP = 0;

    public RpcProtocolDecoder() {
        super(
                MAX_FRAME_LENGTH,
                LENGTH_FIELD_OFFSET,
                LENGTH_FIELD_LENGTH,
                LENGTH_ADJUSTMENT,
                INITIAL_BYTES_TO_STRIP);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        if (null == byteBuf) {
            return null;
        }

        if (byteBuf.readableBytes() <= HEADER_SIZE) {
            return null;
        }

        byteBuf.markReaderIndex();
        int cmdId = byteBuf.readInt();
        byte type = byteBuf.readByte();
        byte zip = byteBuf.readByte();
        int len = byteBuf.readInt();
        if (byteBuf.readableBytes() < len) {
            byteBuf.resetReaderIndex();
            return null;
        }

        byte[] data = new byte[len];
        byteBuf.readBytes(data);
        if (zip == ConstantValues.COMPRESSED) {
            if (log.isInfoEnabled()) {
                log.info(
                        "cmdId = "
                                + cmdId
                                + ". The received packet has been compressed and will be uncompressed before being used.");
            }
            data = CompressUtil.uncompress(data);
        }
        RpcProtocol protocol = new RpcProtocol();
        protocol.setType(type);
        protocol.setData(data);
        protocol.setCmdId(cmdId);
        protocol.setZip(zip);
        return protocol;
    }
}
