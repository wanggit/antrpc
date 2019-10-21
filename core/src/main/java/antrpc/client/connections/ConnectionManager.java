package antrpc.client.connections;

import antrpc.client.Host;

public interface ConnectionManager {
    Connection getConnection(Host host);
}
