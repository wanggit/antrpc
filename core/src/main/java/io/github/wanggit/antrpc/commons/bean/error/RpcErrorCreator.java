package io.github.wanggit.antrpc.commons.bean.error;

public class RpcErrorCreator {

    public static RpcResponseError create(RpcError rpcError) {
        RpcResponseError rpcResponseError = new RpcResponseError();
        rpcResponseError.setCode(rpcError.getCode());
        rpcResponseError.setMessage(rpcError.getMessage());
        return rpcResponseError;
    }

    public static RpcResponseError create(String code, String message) {
        RpcResponseError rpcResponseError = new RpcResponseError();
        rpcResponseError.setCode(code);
        rpcResponseError.setMessage(message);
        return rpcResponseError;
    }
}
