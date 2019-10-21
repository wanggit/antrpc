package antrpc.commons;

import antrpc.client.zk.zknode.ZkNodeBuilder;
import antrpc.client.zk.zknode.ZkNodeType;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public class MethodEqualsTest {

    @Test
    public void testMethodEquals() throws NoSuchMethodException {
        Method method =
                ReflectionUtils.findMethod(
                        ZkNodeBuilder.class, "build", ZkNodeType.Type.class, ChildData.class);
        System.out.println(method);
        Method method2 =
                ReflectionUtils.findMethod(
                        ZkNodeBuilder.class, "build", ZkNodeType.Type.class, ChildData.class);
        System.out.println(method2);
        System.out.println(method.equals(method2));

        method = ZkNodeBuilder.class.getMethod("build", ZkNodeType.Type.class, ChildData.class);
        method2 = ZkNodeBuilder.class.getMethod("build", ZkNodeType.Type.class, ChildData.class);
        System.out.println(method.equals(method2));
    }
}
