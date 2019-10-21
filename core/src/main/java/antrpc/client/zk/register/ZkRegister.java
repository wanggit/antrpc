package antrpc.client.zk.register;

import antrpc.IAntrpcContext;
import antrpc.client.zk.zknode.IZkNodeBuilder;
import antrpc.commons.annotations.RpcMethod;
import antrpc.commons.annotations.RpcService;
import antrpc.commons.constants.ConstantValues;
import antrpc.commons.constants.Constants;
import antrpc.commons.utils.ApplicationNameUtil;
import antrpc.commons.utils.NetUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.List;

/** Register all services identified by @RpcService as RPC services */
@Slf4j
public class ZkRegister implements Register, BeanPostProcessor, ApplicationContextAware {

    private Integer rpcPort;
    private IAntrpcContext antrpcContext;
    private IZkNodeBuilder zkNodeBuilder;
    private IZkRegisterHolder zkRegisterHolder;

    /** */
    @Override
    public void register(RegisterBean registerBean) {
        String zkFullpath = registerBean.getZookeeperFullPath();
        byte[] nodeData = registerBean.getNodeData();
        zkNodeBuilder.remoteCreateZkNode(zkFullpath, nodeData, CreateMode.EPHEMERAL);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        Class<?> beanClass = bean.getClass();
        RpcService rpcService = AnnotationUtils.findAnnotation(beanClass, RpcService.class);
        if (null != rpcService) {
            String className = beanClass.getName();
            List<Class<?>> allInterfaces = ClassUtils.getAllInterfaces(beanClass);
            if (null == allInterfaces || allInterfaces.isEmpty()) {
                throw new BeanCreationException(
                        "The @"
                                + RpcService.class.getSimpleName()
                                + " tagged class must implement an interface.");
            }
            Class<?> interfaceClass = allInterfaces.get(0);
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
            registerBean.setPort(rpcPort);
            zkRegisterHolder.add(registerBean);
            register(registerBean);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }

    private void init() throws BeansException {
        this.zkNodeBuilder = antrpcContext.getZkNodeBuilder();
        this.zkRegisterHolder = antrpcContext.getZkRegisterHolder();
        this.rpcPort = antrpcContext.getConfiguration().getPort();
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
                        + NetUtil.getInstance().getLocalIp()
                        + (null == rpcPort ? "" : ":" + rpcPort);
        RegisterBean.IpNodeDataBean ipNodeDataBean = new RegisterBean.IpNodeDataBean();
        ipNodeDataBean.setAppName(
                ApplicationNameUtil.getApplicationName(
                        antrpcContext.getConfiguration().getEnvironment()));
        ipNodeDataBean.setTs(System.currentTimeMillis());
        ipNodeDataBean.setRpcPort(rpcPort);
        ipNodeDataBean.setHttpPort(
                antrpcContext
                        .getConfiguration()
                        .getEnvironment()
                        .getProperty("server.port", Integer.class));
        byte[] nodeData =
                JSONObject.toJSONString(ipNodeDataBean).getBytes(Charset.forName("UTF-8"));
        zkNodeBuilder.remoteCreateZkNode(fullPath, nodeData, CreateMode.PERSISTENT);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.antrpcContext = applicationContext.getBean(IAntrpcContext.class);
        init();
    }
}
