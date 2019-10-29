package io.github.wanggit.antrpc.client.zk.register;

import io.github.wanggit.antrpc.client.zk.zknode.IZkNodeBuilder;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import org.springframework.beans.BeansException;

public interface Register {
    void register(RegisterBean registerBean, IZkNodeBuilder zkNodeBuilder);

    void init(
            IZkNodeBuilder zkNodeBuilder,
            IZkRegisterHolder registerHolder,
            IConfiguration configuration)
            throws BeansException;
}
