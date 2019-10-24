package io.github.wanggit.antrpc.commons.bean;

import io.github.wanggit.antrpc.commons.constants.ConstantValues;

public class HeartBeatCreator {
    public static RpcProtocol create() {
        RpcProtocol protocol = new RpcProtocol();
        protocol.setCmdId((short) -1);
        protocol.setData(HB_VALUE);
        protocol.setType(ConstantValues.HB_TYPE);
        return protocol;
    }

    private static byte[] HB_VALUE = new byte[1];

    static {
        HB_VALUE[0] = 0x0;
    }
}
