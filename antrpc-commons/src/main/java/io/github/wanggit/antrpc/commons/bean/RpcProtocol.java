package io.github.wanggit.antrpc.commons.bean;

import lombok.Data;

@Data
public class RpcProtocol {
    /** 要发送的数据 */
    private byte[] data;
    /** 业务编号 */
    private int cmdId;
    /** 消息类型 Constants */
    private byte type;

    /**
     * 是否加密 io.github.wanggit.antrpc.commons.constants.ConstantValues#CODECED
     * io.github.wanggit.antrpc.commons.constants.ConstantValues#UNCODCED
     */
    private byte codec;

    /** 是否压缩 ConstantValues#COMPRESSED ConstantValues#UNCOMPRESSED */
    private byte zip;
}
