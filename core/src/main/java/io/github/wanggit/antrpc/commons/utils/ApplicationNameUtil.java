package io.github.wanggit.antrpc.commons.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

@Slf4j
public class ApplicationNameUtil {

    public static String getApplicationName(
            String exposeIp, String appName, Environment environment) {
        return appName + "@" + exposeIp + ":" + environment.getProperty("server.port");
    }
}
