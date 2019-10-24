package io.github.wanggit.antrpc.client.connections;

import io.github.wanggit.antrpc.client.Host;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ConnectionPools {
    public static ConnectionPool getOrCreateConnectionPool(
            Host host,
            ConnectionManager connectionManager,
            GenericObjectPoolConfig<Connection> config) {
        ConnectionPool connectionPool = map.get(host);
        if (null == connectionPool) {
            if (log.isInfoEnabled()) {
                log.info(
                        "Connection pool ["
                                + host
                                + "] does not exist and will be created automatically");
            }
            String lock = host.getIp() + host.getPort();
            synchronized (lock.intern()) {
                connectionPool = map.get(host);
                if (null == connectionPool) {
                    connectionPool = new ConnectionPool(connectionManager, host, config);
                    map.put(host, connectionPool);
                }
            }
        }
        return connectionPool;
    }

    public static ConnectionPool getOrCreateConnectionPool(
            Host host, ConnectionManager connectionManager) {
        return ConnectionPools.getOrCreateConnectionPool(
                host, connectionManager, new GenericObjectPoolConfig<Connection>());
    }

    private static ConcurrentHashMap<Host, ConnectionPool> map =
            new ConcurrentHashMap<Host, ConnectionPool>();
}
