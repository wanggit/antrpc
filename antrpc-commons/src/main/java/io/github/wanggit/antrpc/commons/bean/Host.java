package io.github.wanggit.antrpc.commons.bean;

import lombok.Data;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Objects;

@Data
public class Host {
    private String ip;
    private Integer port;

    public Host() {}

    public Host(String ip, Integer port) {
        if (null == ip || null == port || port <= 0) {
            throw new IllegalArgumentException("ip and port cannot be null.");
        }
        this.ip = ip;
        this.port = port;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Host)) {
            return false;
        }
        Host other = (Host) obj;
        return Objects.equals(other.getIp(), getIp())
                && other.getPort().intValue() == port.intValue();
    }

    public String getHostInfo() {
        return getIp() + ":" + getPort();
    }

    public static Host parse(String hostInfo) {
        if (null == hostInfo) {
            return null;
        }
        String[] tmps = hostInfo.split(":");
        if (tmps.length != 2) {
            throw new IllegalArgumentException("host format is error. it like localhost:port.");
        }
        String ip = tmps[0];
        int port = NumberUtils.toInt(tmps[1], -1);
        if (port == -1) {
            throw new IllegalArgumentException("port must be integer and great than 0.");
        }
        return new Host(ip, port);
    }
}
