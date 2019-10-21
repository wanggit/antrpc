package antrpc.commons.codec;

import antrpc.commons.bean.RpcProtocol;
import antrpc.commons.codec.compress.CompressUtil;
import antrpc.commons.constants.ConstantValues;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcProtocolEncoder extends MessageToByteEncoder<RpcProtocol> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol msg, ByteBuf out) {
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
        out.writeByte(msg.getZip());
        out.writeInt(msg.getData().length);
        out.writeBytes(msg.getData());
    }
}
