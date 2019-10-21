package antrpc.commons.bean;

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
     * 是否压缩 antrpc.commons.constants.ConstantValues#COMPRESSED
     * antrpc.commons.constants.ConstantValues#UNCOMPRESSED
     */
    private byte zip;
}
