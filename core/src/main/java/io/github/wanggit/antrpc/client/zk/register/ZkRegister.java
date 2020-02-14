package io.github.wanggit.antrpc.client.zk.register;

import com.alibaba.fastjson.JSONObject;
import io.github.wanggit.antrpc.client.zk.zknode.IZkNodeBuilder;
import io.github.wanggit.antrpc.commons.annotations.OnRpcFail;
import io.github.wanggit.antrpc.commons.annotations.RpcMethod;
import io.github.wanggit.antrpc.commons.annotations.RpcService;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.github.wanggit.antrpc.commons.constants.Constants;
import io.github.wanggit.antrpc.commons.utils.AntRpcClassUtils;
import io.github.wanggit.antrpc.commons.utils.ApplicationNameUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/** Register all services identified by @RpcService as RPC services */
@Slf4j
public class ZkRegister implements IRegister {

    private final List<RegisterBean> registerBeans = new ArrayList<>();

    /** */
    @Override
    public void register(RegisterBean registerBean, IZkNodeBuilder zkNodeBuilder, String exposeIp) {
        String zkFullpath = registerBean.getZookeeperFullPath(exposeIp);
        byte[] nodeData = registerBean.getNodeData();
        zkNodeBuilder.remoteCreateZkNode(zkFullpath, nodeData, CreateMode.EPHEMERAL);
    }

    // 1
    @Override
    public void checkHasRpcService(Object bean) {
        Class<?> beanClass = bean.getClass();
        RpcService rpcService = AnnotationUtils.findAnnotation(beanClass, RpcService.class);
        OnRpcFail onRpcFail = AnnotationUtils.findAnnotation(beanClass, OnRpcFail.class);
        // 标识OnRpcFail注解的对象不能被注册到注册中心，只能在本地使用
        if (null != rpcService && null == onRpcFail) {
            String className = beanClass.getName();
            List<Class<?>> classes = AntRpcClassUtils.getAllInterfaces(bean);
            if (classes.isEmpty()) {
                throw new BeanCreationException(
                        "The @"
                                + RpcService.class.getSimpleName()
                                + " tagged class must implement an interface.");
            }
            Class<?> interfaceClass = classes.get(0);
            String interfaceClassName = interfaceClass.getName();
            if (log.isInfoEnabled()) {
                log.info(
                        className
                                + " will be registered to zookeeper as "
                                + interfaceClassName
                                + " interface type.");
            }
            Method[] methods =
                    MethodUtils.getMethodsWithAnnotation(beanClass, RpcMethod.class, true, false);
            RegisterBean registerBean = new RegisterBean();
            registerBean.setClassName(interfaceClassName);
            for (Method method : methods) {
                RegisterBean.RegisterBeanMethod registerBeanMethod =
                        RegisterBeanHelper.getRegisterBeanMethod(method);
                registerBean.addMethod(registerBeanMethod);
            }
            registerBeans.add(registerBean);
        }
    }

    // 3
    @Override
    public void init(
            IZkNodeBuilder zkNodeBuilder,
            IZkRegisterHolder zkRegisterHolder,
            IConfiguration configuration)
            throws BeansException {
        Integer rpcPort = configuration.getPort();
        if (null == rpcPort) {
            if (log.isWarnEnabled()) {
                log.warn(
                        "The configured port cannot be found. Check that "
                                + Constants.RPC_PORT_PROP_NAME
                                + " is configured.");
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info("Get to the configuration port is " + rpcPort);
            }
        }
        String fullPath =
                "/"
                        + ConstantValues.ZK_ROOT_NODE_NAME
                        + "/"
                        + configuration.getExposeIp()
                        + (null == rpcPort ? "" : ":" + rpcPort);
        RegisterBean.IpNodeDataBean ipNodeDataBean = new RegisterBean.IpNodeDataBean();
        ipNodeDataBean.setAppName(
                ApplicationNameUtil.getApplicationName(
                        configuration.getExposeIp(),
                        configuration.getApplicationName(),
                        configuration.getEnvironment()));
        ipNodeDataBean.setTs(System.currentTimeMillis());
        ipNodeDataBean.setRpcPort(rpcPort);
        ipNodeDataBean.setHttpPort(
                configuration.getEnvironment().getProperty("server.port", Integer.class));
        byte[] nodeData =
                JSONObject.toJSONString(ipNodeDataBean).getBytes(Charset.forName("UTF-8"));
        zkNodeBuilder.remoteCreateZkNode(fullPath, nodeData, CreateMode.PERSISTENT);

        for (RegisterBean registerBean : registerBeans) {
            registerBean.setPort(rpcPort);
            zkRegisterHolder.add(registerBean);
            register(registerBean, zkNodeBuilder, configuration.getExposeIp());
        }
    }
}
