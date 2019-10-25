package io.github.wanggit.antrpc.test.api;

import io.github.wanggit.antrpc.commons.annotations.RpcMethod;
import io.github.wanggit.antrpc.commons.annotations.RpcService;

@RpcService
public interface UserServiceApi {

    @RpcMethod
    UserDTO findById(Long id);

    UserDTO findBigOne();
}
