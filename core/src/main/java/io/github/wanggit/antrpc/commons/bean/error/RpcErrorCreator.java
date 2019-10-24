package io.github.wanggit.antrpc.commons.bean.error;

public class RpcErrorCreator {

    public static RpcResponseError create(RpcError rpcError) {
        RpcResponseError rpcResponseError = new RpcResponseError();
        rpcResponseError.setCode(rpcError.getCode());
        rpcResponseError.setMessage(rpcError.getMessage());
        return rpcResponseError;
    }
}
