package io.github.wanggit.antrpc.commons.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public abstract class NetUtil {

    public static String getLocalIp() {
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
