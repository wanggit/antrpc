package antrpc.client.zk.register;

import antrpc.commons.constants.ConstantValues;
import antrpc.commons.utils.NetUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Data
public class RegisterBean {

    private String className;

    private List<RegisterBeanMethod> methods = new ArrayList<>();

    private Integer port;

    String getZookeeperFullPath() {
        return "/"
                + ConstantValues.ZK_ROOT_NODE_NAME
                + "/"
                + NetUtil.getInstance().getLocalIp()
                + (null == port ? "" : ":" + port)
                + "/"
                + className;
    }

    byte[] getNodeData() {
        if (!methods.isEmpty()) {
            List<String> methodStrs = new ArrayList<>(methods.size() * 2);
            for (RegisterBeanMethod beanMethod : methods) {
                methodStrs.add(beanMethod.toString());
            }
            InterfaceNodeDataBean interfaceNodeDataBean = new InterfaceNodeDataBean();
            interfaceNodeDataBean.setMethods(methodStrs);
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
    }

    @Data
    public static class IpNodeDataBean {
        private Long ts;
        private String appName;
        private Integer httpPort;
        private Integer rpcPort;
    }
}
