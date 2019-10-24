package io.github.wanggit.antrpc.test.web;

import antrpc.commons.annotations.RpcAutowired;
import io.github.wanggit.antrpc.test.api.UserDTO;
import io.github.wanggit.antrpc.test.api.UserServiceApi;
import org.springframework.stereotype.Component;

@Component
public class UserResource {

    @RpcAutowired private UserServiceApi userServiceApi;

    public UserDTO findById() {
        return userServiceApi.findById(100L);
    }

    public UserDTO findBigOne() {
        return userServiceApi.findBigOne();
    }
}
