package io.github.wanggit.antrpc.server.telnet.handler.command;

import com.alibaba.fastjson.JSONObject;
import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.client.zk.zknode.INodeHostContainer;
import io.github.wanggit.antrpc.client.zk.zknode.NodeHostEntity;
import io.github.wanggit.antrpc.server.telnet.CmdInfoBean;
import io.github.wanggit.antrpc.server.telnet.handler.CmdDesc;
import io.github.wanggit.antrpc.server.telnet.handler.command.util.ArrayClassNameUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/** 可以用于测试注册中心Zookeeper中所有已注册的接口 */
@Slf4j
@CmdDesc(
        value = "test",
        desc =
                "test invoke a method. use $$ to split the parameter list."
                        + "\r\n\t\t\tExample: test io.github.wanggit.antrpc.demo.telnet.api.HelloService#sayHello(java.lang.String)|xianming"
                        + "\r\n\t\t\tExample: test io.github.wanggit.antrpc.demo.telnet.api.TelnetService#test()"
                        + "\r\n\t\t\tExample: test io.github.wanggit.antrpc.demo.telnet.api.HelloService#test(java.lang.String, int, java.lang.Integer)|goubi$$ 3$$ 10"
                        + "\r\n\t\t\tExample: test io.github.wanggit.antrpc.demo.telnet.api.HelloService#doTest(java.lang.String, io.github.wanggit.antrpc.demo.telnet.api.UserDTO)| cao$$ {\"id\": 2, \"name\": \"feng\"}")
public class TestInvokeCmd extends AbsCmd {
    public TestInvokeCmd(IAntrpcContext antrpcContext, Map<String, CmdInfoBean> telnetCmds) {
        super(antrpcContext, telnetCmds);
    }

    @Override
    protected String[] getArguments(String input) {
        int idx = input.indexOf(" ");
        String parameter = input.substring(idx + 1).trim();
        return new String[] {parameter};
    }

    @Override
    protected String intervalDoCmd(String[] arguments) {
        if (arguments.length == 0) {
            return "which method want tested? ";
        }
        Map<String, List<NodeHostEntity>> entitiesSnapshot = null;
        try {
            INodeHostContainer nodeHostContainer = getAntrpcContext().getNodeHostContainer();
            entitiesSnapshot = nodeHostContainer.entitiesSnapshot();
            String fullArgs = arguments[0].trim();
            if (fullArgs.endsWith("|")) {
                fullArgs = fullArgs.substring(0, fullArgs.length() - 1);
            }
            int idx = fullArgs.indexOf("|");
            String fullMethodName = idx == -1 ? fullArgs : fullArgs.substring(0, idx).trim();
            // test(int  , java.lang.Integer) -> test(int,java.lang.Integer)
            fullMethodName = fullMethodName.replaceAll("\\s*,\\s*", ",");
            // testIntArray(java.lang.String, int[]) -> testIntArray(java.lang.String, [I)
            // testIntArray(java.lang.String[], int[]) -> testIntArray([Ljava.lang.String;, [I)
            fullMethodName = ArrayClassNameUtil.replaceArrayClassName(fullMethodName);
            String[] args =
                    idx == -1
                            ? new String[0]
                            : fullArgs.substring(idx + 1).trim().split("\\s*\\$\\$\\s*");
            int idxShape = fullMethodName.indexOf("#");
            String className = fullMethodName.substring(0, idxShape);
            String methodName = fullMethodName.substring(idxShape + 1);
            Class<?> clazz = Class.forName(className);
            Object proxy = getAntrpcContext().getBeanContainer().getOrCreateBean(clazz);
            List<NodeHostEntity> nodeHostEntities = entitiesSnapshot.get(fullMethodName);
            if (null == nodeHostEntities || nodeHostEntities.isEmpty()) {
                return "not found the " + fullMethodName;
            }
            NodeHostEntity nodeHostEntity = nodeHostEntities.get(0);
            Map<String, RegisterBean.RegisterBeanMethod> methodMap = nodeHostEntity.getMethodMap();
            RegisterBean.RegisterBeanMethod registerBeanMethod = methodMap.get(methodName);
            List<String> parameterTypeNames = registerBeanMethod.getParameterTypeNames();
            Class[] parameterTypes = new Class[parameterTypeNames.size()];
            for (int i = 0; i < parameterTypeNames.size(); i++) {
                parameterTypes[i] =
                        ClassUtils.forName(
                                parameterTypeNames.get(i), TestInvokeCmd.class.getClassLoader());
            }
            Method method =
                    ReflectionUtils.findMethod(
                            clazz,
                            methodName.substring(0, methodName.indexOf("(")),
                            parameterTypes);
            if (null == method) {
                return "not found the " + fullMethodName;
            }
            ReflectionUtils.makeAccessible(method);
            Object[] realTypeArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                Class parameterType = parameterTypes[i];
                String value = args[i].trim();
                realTypeArgs[i] = castValueToType(value, parameterType);
            }
            Object result = ReflectionUtils.invokeMethod(method, proxy, realTypeArgs);
            return JSONObject.toJSONString(result);
        } catch (Exception e) {
            return "command format error.";
        } finally {
            if (null != entitiesSnapshot) {
                entitiesSnapshot.clear();
            }
        }
    }

    private Object castValueToType(String value, Class parameterType) {
        if (short.class.equals(parameterType) || Short.class.equals(parameterType)) {
            return Short.parseShort(value);
        }
        if (int.class.equals(parameterType) || Integer.class.equals(parameterType)) {
            return Integer.parseInt(value);
        }
        if (long.class.equals(parameterType) || Long.class.equals(parameterType)) {
            return Long.parseLong(value);
        }
        if (float.class.equals(parameterType) || Float.class.equals(parameterType)) {
            return Float.parseFloat(value);
        }
        if (double.class.equals(parameterType) || Double.class.equals(parameterType)) {
            return Long.parseLong(value);
        }
        if (BigDecimal.class.equals(parameterType)) {
            return new BigDecimal(value);
        }
        if (BigInteger.class.equals(parameterType)) {
            return new BigInteger(value);
        }
        if (String.class.equals(parameterType)) {
            return value;
        }
        if (ClassUtils.isPrimitiveArray(parameterType)
                || ClassUtils.isPrimitiveWrapperArray(parameterType)
                || List.class.equals(parameterType)
                || Map.class.equals(parameterType)
                || Set.class.equals(parameterType)
                || Vector.class.equals(parameterType)) {
            return JSONObject.parseObject(value, parameterType);
        }
        return JSONObject.parseObject(value, parameterType);
    }
}
