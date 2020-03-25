package io.github.wanggit.antrpc.commons.generic;

import io.github.wanggit.antrpc.commons.bean.*;
import io.github.wanggit.antrpc.commons.codec.cryption.ICodec;
import io.github.wanggit.antrpc.commons.codec.cryption.NoOpCodec;
import io.github.wanggit.antrpc.commons.codec.serialize.ISerializer;
import io.github.wanggit.antrpc.commons.codec.serialize.json.JsonSerializer;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.github.wanggit.antrpc.commons.future.ReadClientFuture;
import io.github.wanggit.antrpc.commons.generic.client.DefaultClient;
import io.github.wanggit.antrpc.commons.generic.client.IClient;
import io.github.wanggit.antrpc.commons.zookeeper.ZkNodeType;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
public class DefaultServiceProviderInvoker implements IServiceProviderInvoker {

    private String codecKey;

    private ICodec codec;

    private ISerializer serializer;

    private String zkServers;

    private CuratorFramework curatorFramework;

    private final ConcurrentHashMap<String, CopyOnWriteArraySet<Host>> datas =
            new ConcurrentHashMap<>();

    public DefaultServiceProviderInvoker(ICodec codec, String codecKey, String zkServers) {
        this.zkServers = zkServers;
        if (null != this.zkServers) {
            try {
                initCuratorFramework(this.zkServers);
                zkNodeListener();
            } catch (Exception e) {
                throw new RuntimeException("create DefaultServiceProviderInvoker error.", e);
            }
        }
        this.codec = codec;
        this.codecKey = codecKey;
        this.codec.setKey(codecKey);
        this.serializer = new JsonSerializer();
        this.serializer.setConfigs(new HashMap<>());
        this.serializer.init();
    }

    public DefaultServiceProviderInvoker(String zkServers) {
        this(new NoOpCodec(), null, zkServers);
    }

    public DefaultServiceProviderInvoker() {
        this(null);
    }

    @Override
    public Object invoke(InvokeDTO invokeDTO) {
        RpcRequestBean requestBean = createBasicRpcRequestBean();
        requestBean.setArgumentTypes(invokeDTO.getParameterTypeNames());
        requestBean.setFullClassName(invokeDTO.getInterfaceName());
        requestBean.setMethodName(invokeDTO.getMethodName());
        requestBean.setArgumentValues(invokeDTO.getArgumentValues());
        Host targetHost = invokeDTO.getHost();
        if (null == targetHost && null == zkServers) {
            throw new IllegalArgumentException("Host and zkServers at least one is not empty.");
        }
        if (null == targetHost) {
            CopyOnWriteArraySet<Host> copyOnWriteArraySet = datas.get(invokeDTO.getInterfaceName());
            int count = 0;
            int max = 10;
            while (null == copyOnWriteArraySet && count < max) {
                if (log.isWarnEnabled()) {
                    log.warn(
                            "Not found the "
                                    + invokeDTO.getInterfaceName()
                                    + " service provider. "
                                    + count
                                    + " times. ");
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    if (log.isErrorEnabled()) {
                        log.error("wait error.", e);
                    }
                }
                count++;
                copyOnWriteArraySet = datas.get(invokeDTO.getInterfaceName());
            }
            if (null == copyOnWriteArraySet && count >= max) {
                if (log.isErrorEnabled()) {
                    log.error("Not found " + invokeDTO.getInterfaceName() + " service provider.");
                }
                throw new IllegalArgumentException("zkServers maybe is wrong.");
            }
            List<Host> hosts = new ArrayList<>(copyOnWriteArraySet);
            Collections.shuffle(hosts);
            targetHost = hosts.get(0);
        }
        RpcProtocol protocol = new RpcProtocol();
        protocol.setCmdId(IdGenHelper.getInstance().getId());
        protocol.setType(ConstantValues.BIZ_TYPE);
        protocol.setSerializer(ConstantValues.JSON_SERIALIZER);
        protocol.setData(serializer.serialize(requestBean));
        IClient client;
        try {
            client = new DefaultClient(targetHost, codec, serializer);
        } catch (InterruptedException e) {
            if (log.isErrorEnabled()) {
                log.error("create DefaultClient ERROR.", e);
            }
            throw new RuntimeException();
        }
        ReadClientFuture future = client.send(protocol);
        RpcResponseBean responseBean = future.get();
        return responseBean.getResult();
    }

    private RpcRequestBean createBasicRpcRequestBean() {
        RpcRequestBean rpcRequestBean = new RpcRequestBean();
        rpcRequestBean.setTs(System.currentTimeMillis());
        rpcRequestBean.setOneway(false);
        rpcRequestBean.setCaller(ConstantValues.GENERIC_CALLER);
        rpcRequestBean.setId(IdGenHelper.getInstance().getUUID());
        rpcRequestBean.setSerialNumber(IdGenHelper.getInstance().getUUID());
        return rpcRequestBean;
    }

    private void initCuratorFramework(String zkServers) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5, 30000);
        curatorFramework =
                CuratorFrameworkFactory.builder()
                        .connectString(zkServers)
                        .connectionTimeoutMs(5000)
                        .retryPolicy(retryPolicy)
                        .build();
        curatorFramework.start();
    }

    private void zkNodeListener() throws Exception {
        TreeCache treeCache =
                new TreeCache(curatorFramework, "/" + ConstantValues.ZK_ROOT_NODE_NAME);
        treeCache
                .getListenable()
                .addListener(
                        (client, event) -> {
                            if (TreeCacheEvent.Type.NODE_ADDED.equals(event.getType())
                                    || TreeCacheEvent.Type.NODE_UPDATED.equals(event.getType())
                                    || TreeCacheEvent.Type.NODE_REMOVED.equals(event.getType())) {
                                ChildData childData = event.getData();
                                if (log.isInfoEnabled()) {
                                    log.info(
                                            event.getType().name() + " --> " + childData.getPath());
                                }
                                ZkNodeType.Type type = ZkNodeType.getType(childData.getPath());
                                if (ZkNodeType.Type.INTERFACE.equals(type)) {
                                    String path = childData.getPath();
                                    String subPath =
                                            path.replaceFirst(
                                                    "/" + ConstantValues.ZK_ROOT_NODE_NAME + "/",
                                                    "");
                                    int idx = subPath.indexOf("/");
                                    String hostInfo = subPath.substring(0, idx);
                                    String interfaceName = subPath.substring(idx + 1);
                                    Host host = Host.parse(hostInfo);
                                    if (TreeCacheEvent.Type.NODE_ADDED.equals(event.getType())
                                            || TreeCacheEvent.Type.NODE_UPDATED.equals(
                                                    event.getType())) {
                                        if (!datas.containsKey(interfaceName)) {
                                            datas.put(interfaceName, new CopyOnWriteArraySet<>());
                                        }
                                        datas.get(interfaceName).add(host);
                                    } else if (TreeCacheEvent.Type.NODE_REMOVED.equals(
                                            event.getType())) {
                                        Set<Host> hosts = datas.get(interfaceName);
                                        if (null != hosts) {
                                            hosts.remove(host);
                                        }
                                    }
                                }
                            }
                        });
        treeCache.start();
    }
}
