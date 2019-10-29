package io.github.wanggit.antrpc.commons.test;

import io.github.wanggit.antrpc.client.zk.ZkClient;
import io.github.wanggit.antrpc.commons.config.Configuration;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.github.wanggit.antrpc.commons.utils.NetUtil;

public abstract class ZkNodeTestUtil {

    public static void afterClear(Integer port) throws Exception {
        String path =
                "/"
                        + ConstantValues.ZK_ROOT_NODE_NAME
                        + "/"
                        + NetUtil.getInstance().getLocalIp()
                        + ":"
                        + port;
        System.out.println(">>>>>>>>>>>>>>>>>>> " + path);
        new ZkClient(new Configuration())
                .getCurator()
                .delete()
                .deletingChildrenIfNeeded()
                .forPath(path);
    }
}
