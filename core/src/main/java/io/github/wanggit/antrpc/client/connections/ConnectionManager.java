package io.github.wanggit.antrpc.client.connections;

import io.github.wanggit.antrpc.client.Host;

public interface ConnectionManager {
    Connection getConnection(Host host);

    void addConnectionPool(Host host, ConnectionPool connectionPool);
}
