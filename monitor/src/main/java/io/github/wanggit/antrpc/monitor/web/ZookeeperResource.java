package io.github.wanggit.antrpc.monitor.web;

import com.alibaba.fastjson.JSONObject;
import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.client.zk.zknode.NodeHostEntity;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.github.wanggit.antrpc.monitor.web.vo.RegisterAppVO;
import io.github.wanggit.antrpc.monitor.web.vo.Result;
import io.github.wanggit.antrpc.monitor.web.vo.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/zk")
public class ZookeeperResource {

    @Autowired private IAntrpcContext antrpcContext;

    @GetMapping("/sub_nodes")
    public Result<ArrayList<String>> getSubNodes(@RequestParam(value = "path") String path) {
        if (null == path) {
            return new Result<>(ResultCode.ERROR);
        }
        CuratorFramework curator = antrpcContext.getZkClient().getCurator();
        try {
            List<String> paths = curator.getChildren().forPath(path);
            return new Result<>(new ArrayList<>(paths));
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
        return new Result<>(ResultCode.ERROR);
    }

    /**
     * 获取节点上的数据
     *
     * @param path
     * @return
     */
    @GetMapping("/node_data")
    public Result<String> getNodeData(@RequestParam(value = "path") String path) {
        if (null == path) {
            return new Result<>(ResultCode.ERROR);
        }
        CuratorFramework curator = antrpcContext.getZkClient().getCurator();
        try {
            byte[] bytes = curator.getData().forPath(path);
            return new Result<>(new String(bytes));
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
        return new Result<>(ResultCode.ERROR);
    }

    /**
     * 获取所有远程接口
     *
     * @return
     */
    @GetMapping("/all_interfaces")
    public Result<HashMap<String, List<NodeHostEntity>>> allInterfaces() {
        return new Result<>(new HashMap<>(antrpcContext.getNodeHostContainer().snapshot()));
    }

    /**
     * 获取一个接口的各提供者信息
     *
     * @param className
     * @return
     */
    @GetMapping("/more_interface")
    public Result<ArrayList<NodeHostEntity>> moreInterface(String className) {
        return new Result<>(
                new ArrayList<>(antrpcContext.getNodeHostContainer().getHostEntities(className)));
    }

    /**
     * 查询所以被注册的应用
     *
     * @return
     */
    @GetMapping("/all_registed_apps")
    public Result<ArrayList<RegisterAppVO>> findAllRegistedApp() {
        CuratorFramework curator = antrpcContext.getZkClient().getCurator();
        String rootPath = "/" + ConstantValues.ZK_ROOT_NODE_NAME;
        ArrayList<RegisterAppVO> registerAppVOS = new ArrayList<>();
        try {
            List<String> subPaths = curator.getChildren().forPath(rootPath);
            for (String it : subPaths) {
                byte[] bytes = curator.getData().forPath(rootPath + "/" + it);
                String json = new String(bytes, Charset.forName("UTF-8"));
                RegisterBean.IpNodeDataBean ipNodeDataBean =
                        JSONObject.parseObject(json, RegisterBean.IpNodeDataBean.class);
                RegisterAppVO registerAppVO = new RegisterAppVO();
                registerAppVO.setAppName(ipNodeDataBean.getAppName());
                registerAppVO.setHttpPort(ipNodeDataBean.getHttpPort());
                registerAppVO.setRpcPort(ipNodeDataBean.getRpcPort());
                registerAppVO.setRefreshTs(ipNodeDataBean.getTs());
                registerAppVOS.add(registerAppVO);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("An exception occurred while querying all registered applications.", e);
            }
            return new Result<>(ResultCode.ERROR);
        }
        return new Result<>(registerAppVOS);
    }
}
