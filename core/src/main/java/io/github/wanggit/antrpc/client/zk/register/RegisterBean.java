package io.github.wanggit.antrpc.client.zk.register;

import com.alibaba.fastjson.JSONObject;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RegisterBean {

    private String className;

    private List<RegisterBeanMethod> methods = new ArrayList<>();

    private Integer port;

    String getZookeeperFullPath(String exposeIp) {
        return "/"
                + ConstantValues.ZK_ROOT_NODE_NAME
                + "/"
                + exposeIp
                + (null == port ? "" : ":" + port)
                + "/"
                + className;
    }

    byte[] getNodeData() {
        if (!methods.isEmpty()) {
            List<String> methodStrs = new ArrayList<>(methods.size() * 2);
            Map<String, RegisterBeanMethod> methodMap = new HashMap<>();
            for (RegisterBeanMethod beanMethod : methods) {
                String key = beanMethod.toString();
                methodStrs.add(key);
                methodMap.put(key, beanMethod);
            }
            InterfaceNodeDataBean interfaceNodeDataBean = new InterfaceNodeDataBean();
            interfaceNodeDataBean.setMethods(methodStrs);
            interfaceNodeDataBean.setMethodMap(methodMap);
            interfaceNodeDataBean.setTs(System.currentTimeMillis());
            String json = JSONObject.toJSONString(interfaceNodeDataBean);
            return json.getBytes(Charset.forName("UTF-8"));
        } else {
            return JSONObject.toJSONString(new InterfaceNodeDataBean())
                    .getBytes(Charset.forName("UTF-8"));
        }
    }

    void addMethod(RegisterBeanMethod method) {
        this.methods.add(method);
    }

    @Data
    public static class RegisterBeanMethod {
        private String methodName;

        private List<String> parameterTypeNames;

        private int limit;

        private int durationInSeconds;

        @Override
        public String toString() {
            return methodName
                    + "("
                    + (null == parameterTypeNames ? "" : StringUtils.join(parameterTypeNames, ","))
                    + ")";
        }
    }

    @Data
    public static class InterfaceNodeDataBean {
        private Long ts;
        private List<String> methods;
        private Map<String, RegisterBeanMethod> methodMap;
    }

    @Data
    public static class IpNodeDataBean {
        private Long ts;
        private String appName;
        private Integer httpPort;
        private Integer rpcPort;
    }
}
