package io.github.wanggit.antrpc.commons.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApplicationNameUtil {

    public static String getApplicationName(String exposeIp, String appName, int rpcPort) {
        return appName + "@" + exposeIp + ":" + rpcPort;
    }
}
