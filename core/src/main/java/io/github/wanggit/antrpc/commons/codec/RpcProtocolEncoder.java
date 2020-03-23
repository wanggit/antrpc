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

/**
 *
 *
 * <pre>
 *     +----------------------------------------------------------+---------------------------------+
 *     |                   header 16 bytes                        |   body max 1024 * 1024 bytes    |
 *     +---------+---------+---------+------------+---------------+-------------+-------------------+
 *     | cmdId   | Type    | Codec   | Compressed | Reserve Space | Body Length |    Body Content   |
 *     | 4 bytes | 1 bytes | 1 bytes | 1 bytes    |    9 bytes    |   4 bytes   |                   |
 *     +---------+---------+---------+------------+---------------+-------------+-------------------+
 * </pre>
 *
 * <pre>
 * <h3>headers total 16 bytes</h3>
 * cmdId: 命令ID，int 4个字节.<br/>
 * Type: 心跳类型或数据包类型 1个字节.<br/>
 * Codec: 是否加密 1个字节.<br/>
 * Compressed: 是否被压缩 1个字节.<br/>
 * Reserve Space: 协议头预留空间，还剩余9个字节.<br/>
 *
 * <h3>body max 1024 * 1024 bytes</h3>
 * Body Length: 数据包长度 4个字节.<br/>
 * Body Content: 数据包.<br/>
 * </pre>
 */
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
            // 9个字节的预留空间
            out.writeBytes(new byte[9]);
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
            // 9个字节的预留空间
            out.writeBytes(new byte[9]);
            out.writeInt(msg.getData().length);
            out.writeBytes(msg.getData());
        }
    }
}
