package io.github.wanggit.antrpc.client.zk.register;

import io.github.wanggit.antrpc.client.zk.zknode.IZkNodeBuilder;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import org.springframework.beans.BeansException;

public interface IRegister {
    void register(RegisterBean registerBean, IZkNodeBuilder zkNodeBuilder, String exposeIp);

    void init(
            IZkNodeBuilder zkNodeBuilder,
            IZkRegisterHolder registerHolder,
            IConfiguration configuration)
            throws BeansException;
}
