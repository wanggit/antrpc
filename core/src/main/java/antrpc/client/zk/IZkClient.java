package antrpc.client.zk;

import org.apache.curator.framework.CuratorFramework;

public interface IZkClient {
    CuratorFramework getCurator();
}
