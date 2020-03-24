package io.github.wanggit.antrpc.commons.codec;

import io.github.wanggit.antrpc.commons.bean.RpcProtocol;
import io.github.wanggit.antrpc.commons.codec.compress.CompressUtil;
import io.github.wanggit.antrpc.commons.codec.cryption.ICodec;
import io.github.wanggit.antrpc.commons.config.CodecConfig;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcProtocolDecoder extends LengthFieldBasedFrameDecoder {

    private CodecConfig codecConfig;
    private ICodec codec;

    public RpcProtocolDecoder(CodecConfig codecConfig, ICodec codec) {
        super(
                ConstantValues.DECODER_MAX_FRAME_LENGTH,
                ConstantValues.DECODER_LENGTH_FIELD_OFFSET,
                ConstantValues.DECODER_LENGTH_FIELD_LENGTH,
                ConstantValues.DECODER_LENGTH_ADJUSTMENT,
                ConstantValues.DECODER_INITIAL_BYTES_TO_STRIP);
        this.codecConfig = codecConfig;
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
        byte wasCodec = byteBuf.readByte();
        byte zip = byteBuf.readByte();
        byte serialzer = byteBuf.readByte();
        // 因为header还冗余8个字节，所以再读8个字节
        byteBuf.readBytes(8);
        // 读body长度
        int len = byteBuf.readInt();
        if (byteBuf.readableBytes() < len) {
            byteBuf.resetReaderIndex();
            return null;
        }
        byte[] data = new byte[len];
        byteBuf.readBytes(data);
        if (type == ConstantValues.HB_TYPE) {
            RpcProtocol protocol = new RpcProtocol();
            protocol.setType(type);
            protocol.setCodec(wasCodec);
            protocol.setData(data);
            protocol.setCmdId(cmdId);
            protocol.setZip(zip);
            protocol.setSerializer(serialzer);
            return protocol;
        } else if (type == ConstantValues.BIZ_TYPE) {
            if (zip == ConstantValues.COMPRESSED) {
                if (log.isInfoEnabled()) {
                    log.info(
                            "cmdId = "
                                    + cmdId
                                    + ". The received packet has been compressed and will be uncompressed before being used.");
                }
                data = CompressUtil.uncompress(data);
            }
            if (wasCodec == ConstantValues.CODECED) {
                data = codec.decrypt(data);
            }
            RpcProtocol protocol = new RpcProtocol();
            protocol.setType(type);
            protocol.setCodec(wasCodec);
            protocol.setData(data);
            protocol.setCmdId(cmdId);
            protocol.setZip(zip);
            protocol.setSerializer(serialzer);
            return protocol;
        }
        return null;
    }
}
