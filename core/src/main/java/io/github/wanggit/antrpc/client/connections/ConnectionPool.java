package io.github.wanggit.antrpc.client.connections;

import io.github.wanggit.antrpc.client.Host;
import io.github.wanggit.antrpc.commons.bean.HeartBeatCreator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class ConnectionPool {

    private static final int DEFAULT_TIMEOUT = 5000;
    private GenericObjectPool<Connection> pool;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    ConnectionPool(
            ConnectionManager connectionManager,
            Host host,
            GenericObjectPoolConfig<Connection> config) {
        pool =
                new GenericObjectPool<Connection>(
                        new ConnectionFactory(connectionManager, host), config);
        initPool(config.getMinIdle());
        executor.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        printStat();
                    }
                },
                500,
                1000,
                TimeUnit.MILLISECONDS);
    }

    public ConnectionPool(ConnectionManager connectionManager, Host host) {
        this(connectionManager, host, new GenericObjectPoolConfig<Connection>());
    }

    private void initPool(int cnt) {
        if (log.isInfoEnabled()) {
            log.info("Initialize " + cnt + " connections.");
        }
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(cnt);
        List<Future<Connection>> futures = new ArrayList<Future<Connection>>();

        for (int i = 0; i < cnt; i++) {
            Future<Connection> future =
                    fixedThreadPool.submit(
                            new Callable<Connection>() {
                                @Override
                                public Connection call() throws Exception {
                                    return pool.borrowObject();
                                }
                            });
            futures.add(future);
        }

        List<Connection> connections = new ArrayList<Connection>();
        for (Future<Connection> future : futures) {
            try {
                Connection connection = future.get(10, TimeUnit.SECONDS);
                connection.send(HeartBeatCreator.create());
                connections.add(connection);
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("Failed to initialize the connection, delayed until use.");
                }
            }
        }

        for (Connection connection : connections) {
            pool.returnObject(connection);
        }
        fixedThreadPool.shutdown();
        if (log.isInfoEnabled()) {
            log.info("Initialization connection completed.");
        }
    }

    private Connection borrow(long timeout) {
        try {
            return pool.borrowObject(timeout);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to getId a connection from the connection pool.", e);
            }
            throw new RuntimeException(e);
        }
    }

    public Connection borrow() {
        return borrow(DEFAULT_TIMEOUT);
    }

    public void returnObject(Connection connection) {
        pool.returnObject(connection);
    }

    public void invalidateObject(Connection connection) {
        try {
            pool.invalidateObject(connection);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            throw new RuntimeException(e);
        }
    }

    private void printStat() {
        if (log.isTraceEnabled()) {
            log.trace(
                    "CreatedCount="
                            + pool.getCreatedCount()
                            + ", ReturnedCount="
                            + pool.getReturnedCount()
                            + ", BorrowedCount="
                            + pool.getBorrowedCount()
                            + ", DestroyedCount="
                            + pool.getDestroyedCount());
        }
    }
}
