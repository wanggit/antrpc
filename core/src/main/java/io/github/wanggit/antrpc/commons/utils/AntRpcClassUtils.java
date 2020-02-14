package io.github.wanggit.antrpc.commons.utils;

import org.springframework.cglib.proxy.Factory;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AntRpcClassUtils {

    public static List<Class<?>> getAllInterfaces(Object bean) {
        Class<?>[] allInterfaces = ClassUtils.getAllInterfaces(bean);
        List<Class<?>> classes = Arrays.asList(allInterfaces);
        // 如果是Cglib的动态代理，要把Cglib的Factory接口排除在外
        if (ClassUtils.isCglibProxy(bean)) {
            classes =
                    classes.stream()
                            .filter(aClass -> !Factory.class.equals(aClass))
                            .collect(Collectors.toList());
        }
        return classes;
    }
}
