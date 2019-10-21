package antrpc.test.service;

import antrpc.test.api.UserDTO;
import antrpc.test.api.UserServiceApi;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

@Component
public class UserServiceImpl implements UserServiceApi {
    @Override
    public UserDTO findById(Long id) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(100L);
        userDTO.setName("wanggang");
        userDTO.setRemark("remark");
        return userDTO;
    }

    @Override
    public UserDTO findBigOne() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1000L);
        userDTO.setName(RandomStringUtils.randomAlphanumeric(1024));
        userDTO.setRemark(RandomStringUtils.randomAlphanumeric(1024));
        return userDTO;
    }
}
