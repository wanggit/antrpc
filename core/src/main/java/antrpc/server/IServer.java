package antrpc.server;

public interface IServer {
    void open(Integer port) throws InterruptedException;

    void open() throws InterruptedException;

    void close();

    boolean isActive();
}
