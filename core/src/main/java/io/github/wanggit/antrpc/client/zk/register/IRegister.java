package io.github.wanggit.antrpc.client.zk.register;

import io.github.wanggit.antrpc.client.zk.zknode.IZkNodeBuilder;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import org.springframework.beans.BeansException;

import java.util.List;

public interface IRegister {
    void register(RegisterBean registerBean, IZkNodeBuilder zkNodeBuilder, String exposeIp);

    void unregister(IConfiguration configuration, IZkNodeBuilder zkNodeBuilder);

    List<RegisterBean> snapshot();

    /**
     * 把所有RegisterBean
     * 对象中的pause设置为false，io.github.wanggit.antrpc.client.zk.register.ZkRegisterHolder自动完成注册
     */
    void playAllRegister();

    /**
     * 把某个RegisterBean对象中的pause设置为false,
     * io.github.wanggit.antrpc.client.zk.register.ZkRegisterHolder自动完成注册
     *
     * @param className className
     */
    void playRegister(String className);

    /**
     * 把所有 RegisterBean 对象中的pause设置为true 暂停此Bean的注册，并把已注册节点全部删除
     *
     * @param configuration configuration
     * @param zkNodeBuilder zkNodeBuilder
     */
    void pauseAllRegister(IConfiguration configuration, IZkNodeBuilder zkNodeBuilder);

    /**
     * 把某一个 RegitserBean 对象中的pause设置为true 暂停此Bean的注册，并把已注册的此节点删除
     *
     * @param configuration configuration
     * @param zkNodeBuilder zkNodeBuilder
     * @param className className
     */
    void pauseRegister(
            IConfiguration configuration, IZkNodeBuilder zkNodeBuilder, String className);

    RegisterBean findRegisterBeanByClassName(String className);

    // 1
    void checkHasRpcService(Object bean);

    void init(
            IZkNodeBuilder zkNodeBuilder,
            IZkRegisterHolder registerHolder,
            IConfiguration configuration)
            throws BeansException;
}
