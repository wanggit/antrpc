package io.github.wanggit.antrpc.commons.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

@Slf4j
public class ApplicationNameUtil {

    public static String getApplicationName(Environment environment) {
        String appName = environment.getProperty("spring.application.name");
        if (null == appName) {
            throw new RuntimeException("spring.application.name is not configured,");
        }
        return appName
                + "@"
                + NetUtil.getInstance().getLocalIp()
                + ":"
                + environment.getProperty("server.port");
    }
}
