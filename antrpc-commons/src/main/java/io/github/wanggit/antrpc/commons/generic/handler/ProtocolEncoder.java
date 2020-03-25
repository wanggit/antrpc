package io.github.wanggit.antrpc.commons.generic.handler;

import io.github.wanggit.antrpc.commons.bean.RpcProtocol;
import io.github.wanggit.antrpc.commons.codec.compress.CompressUtil;
import io.github.wanggit.antrpc.commons.codec.cryption.ICodec;
import io.github.wanggit.antrpc.commons.codec.serialize.ISerializer;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 *
 *
 * <pre>
 * +--------------------------------------------------------------------------+---------------------------------+
 * |                           header 16 bytes                                |   body max 1024 * 1024 bytes    |
 * +---------+---------+---------+------------+---------------+---------------+-------------+-------------------+
 * | cmdId   |   Type  | Codec   | Compressed |  Serializer   | Reserve Space | Body Length |    Body Content   |
 * | 4 bytes | 1 bytes | 1 bytes | 1 bytes    |    1 bytes    |    8 bytes    |   4 bytes   |                   |
 * +---------+---------+---------+------------+---------------+---------------+-------------+-------------------+
 * </pre>
 *
 * <pre>
 * <h3>headers total 16 bytes</h3>
 * cmdId: 命令ID，int 4个字节.<br/>
 * Type: 心跳类型或数据包类型 1个字节.<br/>
 * Codec: 是否加密 1个字节.<br/>
 * Compressed: 是否被压缩 1个字节.<br/>
 * Serializer: 本次请求的序列化类型. <br/>
 * Reserve Space: 协议头预留空间，还剩余8个字节.<br/>
 *
 * <h3>body max 1024 * 1024 bytes</h3>
 * Body Length: 数据包长度 4个字节.<br/>
 * Body Content: 数据包.<br/>
 * </pre>
 */
public class ProtocolEncoder extends MessageToByteEncoder<RpcProtocol> {

    private final ICodec codec;
    private final ISerializer serializer;

    public ProtocolEncoder(ICodec codec, ISerializer serializer) {
        this.codec = codec;
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol msg, ByteBuf out)
            throws Exception {
        if (null != codec) {
            msg.setCodec(ConstantValues.CODECED);
            msg.setData(codec.encrypt(msg.getData()));
        } else {
            msg.setCodec(ConstantValues.UNCODCED);
        }
        if (msg.getData().length > ConstantValues.NEED_COMPRESS_LENGTH) {
            msg.setZip(ConstantValues.COMPRESSED);
            msg.setData(CompressUtil.compress(msg.getData()));
        } else {
            msg.setZip(ConstantValues.UNCOMPRESSED);
        }
        out.writeInt(msg.getCmdId());
        out.writeByte(msg.getType());
        out.writeByte(msg.getCodec());
        out.writeByte(msg.getZip());
        out.writeByte(msg.getSerializer());
        out.writeBytes(new byte[8]);
        out.writeInt(msg.getData().length);
        out.writeBytes(msg.getData());
    }
}
