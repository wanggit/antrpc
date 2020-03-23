package io.github.wanggit.antrpc.commons.future;

import io.github.wanggit.antrpc.commons.bean.RpcProtocol;
import io.github.wanggit.antrpc.commons.codec.serialize.ISerializer;

import java.util.concurrent.ConcurrentHashMap;

public class ReadClientFutureHolder {

    private static ConcurrentHashMap<Integer, ReadClientFuture> holders =
            new ConcurrentHashMap<>(Short.MAX_VALUE, 0.75f, Short.MAX_VALUE);

    public static void receive(RpcProtocol rpcProtocol) {
        ReadClientFuture future = holders.remove(rpcProtocol.getCmdId());
        if (null == future) {
            throw new RuntimeException(
                    rpcProtocol.getCmdId() + " was received. but future is null.");
        } else {
            future.receive(rpcProtocol);
        }
    }

    public static ReadClientFuture createFuture(int cmdId, ISerializer serializer) {
        ReadClientFuture future = new ReadClientFuture(serializer);
        holders.put(cmdId, future);
        return future;
    }

    public static void removeFuture(int cmdId) {
        holders.remove(cmdId);
    }
}
