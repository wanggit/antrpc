package io.github.wanggit.antrpc.client.spring;

public interface IOnFailProcessor {
    // 3
    void init(IOnFailHolder onFailHolder);

    // 1
    void checkHasOnRpcFail(Object bean);
}
