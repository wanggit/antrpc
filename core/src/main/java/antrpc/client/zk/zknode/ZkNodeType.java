package antrpc.client.zk.zknode;

public class ZkNodeType {

    public enum Type {
        ROOT,
        IP,
        INTERFACE
    }

    public static Type getType(String path) {
        if (null == path) {
            throw new IllegalArgumentException("path cannot be null.");
        }
        int level = path.split("/").length - 1;
        if (level == 1) {
            return Type.ROOT;
        } else if (level == 2) {
            return Type.IP;
        } else if (level == 3) {
            return Type.INTERFACE;
        }
        return null;
    }
}
