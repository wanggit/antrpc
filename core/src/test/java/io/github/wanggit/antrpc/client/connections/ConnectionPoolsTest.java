package io.github.wanggit.antrpc.client.connections;

import io.github.wanggit.antrpc.client.Host;
import io.github.wanggit.antrpc.commons.bean.RpcProtocol;
import io.github.wanggit.antrpc.commons.test.WaitUtil;
import io.netty.channel.local.LocalServerChannel;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Assert;
import org.junit.Test;

public class ConnectionPoolsTest {

    @Test
    public void testGetOrCreateConnectionPool() throws Exception {
        ConnectionManager connectionManager =
                new ConnectionManager() {
                    @Override
                    public Connection getConnection(Host host) {
                        return new DefaultConnection(new LocalServerChannel(), null) {
                            @Override
                            public void send(RpcProtocol rpcProtocol) {
                                System.out.println("mock connection send.....");
                            }
                        };
                    }

                    @Override
                    public void addConnectionPool(Host host, ConnectionPool connectionPool) {}
                };
        GenericObjectPoolConfig<Connection> config = new GenericObjectPoolConfig<>();
        config.setMinIdle(4);
        config.setMaxTotal(10);
        ConnectionPool connectionPool =
                ConnectionPools.getOrCreateConnectionPool(
                        Host.parse("localhost:2181"), connectionManager, config);
        for (int i = 0; i < 50; i++) {
            new Thread() {
                @Override
                public void run() {
                    Connection connection = null;
                    try {
                        connection = connectionPool.borrow();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    Assert.assertNotNull(connection);
                    connection.send(null);
                    connectionPool.returnObject(connection);
                }
            }.start();
        }
        WaitUtil.wait(5, 1);
    }
}

// Generated with love by TestMe :) Please report issues and submit feature requests at:
// http://weirddev.com/forum#!/testme
