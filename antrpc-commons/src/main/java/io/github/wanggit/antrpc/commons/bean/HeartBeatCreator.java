package io.github.wanggit.antrpc.commons.bean;

import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import org.apache.commons.lang3.RandomUtils;

public class HeartBeatCreator {
    public static RpcProtocol create() {
        return create(RandomUtils.nextInt());
    }

    public static RpcProtocol create(int cmdId) {
        RpcProtocol protocol = new RpcProtocol();
        protocol.setCmdId(cmdId);
        protocol.setData(HB_VALUE);
        protocol.setType(ConstantValues.HB_TYPE);
        return protocol;
    }

    private static byte[] HB_VALUE = new byte[1];

    static {
        HB_VALUE[0] = 0x0;
    }
}
