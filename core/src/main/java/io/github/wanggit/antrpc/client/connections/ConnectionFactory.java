package io.github.wanggit.antrpc.client.connections;

import io.github.wanggit.antrpc.client.Host;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ConnectionFactory extends BasePooledObjectFactory<Connection> {

    private ConnectionManager connectionManager;
    private Host host;

    ConnectionFactory(ConnectionManager connectionManager, Host host) {
        this.connectionManager = connectionManager;
        this.host = host;
    }

    @Override
    public Connection create() throws Exception {
        if (null == connectionManager || null == host) {
            throw new IllegalArgumentException("connectionManager and host cannot be null.");
        }
        return connectionManager.getConnection(host);
    }

    @Override
    public PooledObject<Connection> wrap(Connection obj) {
        return new DefaultPooledObject<Connection>(obj);
    }
}
