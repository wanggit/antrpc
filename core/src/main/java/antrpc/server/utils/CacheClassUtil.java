package antrpc.server.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CacheClassUtil {

    private ConcurrentHashMap<String, Class> classMap = new ConcurrentHashMap<>();

    private CacheClassUtil() {}

    private static final CacheClassUtil instance = new CacheClassUtil();

    public static CacheClassUtil getInstance() {
        return instance;
    }

    public Class getCacheClass(String className) throws ClassNotFoundException {
        if (null == className) {
            throw new IllegalArgumentException("className cannot be null.");
        }
        if (!classMap.containsKey(className)) {
            synchronized (className.intern()) {
                if (!classMap.containsKey(className)) {
                    try {
                        Class<?> clazz = ClassUtils.getClass(className);
                        classMap.put(className, clazz);
                    } catch (ClassNotFoundException e) {
                        if (log.isErrorEnabled()) {
                            log.error("Not found " + className, e);
                        }
                        throw e;
                    }
                }
            }
        }
        return classMap.get(className);
    }
}
