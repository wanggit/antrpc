package io.github.wanggit.antrpc.commons.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class NetUtil {

    private String localIp = null;
    private static NetUtil instance = new NetUtil();

    private NetUtil() {
        if (null == localIp) {
            localIp = initLocalIp();
        }
    }

    public static NetUtil getInstance() {
        return instance;
    }

    public String getLocalIp() {
        return localIp;
    }

    private static String initLocalIp() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            if (log.isErrorEnabled()) {
                log.error("Unable to obtain native IP address.", e);
            }
            throw new RuntimeException();
        }
    }
}
