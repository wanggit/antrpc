package antrpc.test.web;

import antrpc.commons.annotations.RpcAutowired;
import antrpc.test.api.UserDTO;
import antrpc.test.api.UserServiceApi;
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
