package antrpc.test.api;

import antrpc.commons.annotations.RpcMethod;
import antrpc.commons.annotations.RpcService;

@RpcService
public interface UserServiceApi {

    @RpcMethod
    UserDTO findById(Long id);

    UserDTO findBigOne();
}
