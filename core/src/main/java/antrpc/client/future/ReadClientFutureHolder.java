package antrpc.client.future;

import antrpc.commons.bean.RpcProtocol;

import java.util.concurrent.ConcurrentHashMap;

public class ReadClientFutureHolder {

    private static ConcurrentHashMap<Integer, ReadClientFuture> holders =
            new ConcurrentHashMap<>(Short.MAX_VALUE, 0.75f, Short.MAX_VALUE);

    public static void receive(final RpcProtocol rpcProtocol) {
        ReadClientFuture future = holders.remove(rpcProtocol.getCmdId());
        if (null == future) {
            throw new RuntimeException(
                    rpcProtocol.getCmdId() + " was received. but future is null.");
        } else {
            future.receive(rpcProtocol);
        }
    }

    public static ReadClientFuture createFuture(int cmdId) {
        ReadClientFuture future = new ReadClientFuture();
        holders.put(cmdId, future);
        return future;
    }

    public static void removeFuture(int cmdId) {
        holders.remove(cmdId);
    }
}
