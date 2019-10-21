package antrpc.client.zk.zknode;

import antrpc.client.Host;

import java.util.List;

/** 用于直连的Host对象，此对象中只包含 ip 与 port */
public class DirectNodeHostEntity extends NodeHostEntity {

    public static DirectNodeHostEntity from(Host host) {
        DirectNodeHostEntity directNodeHostEntity = new DirectNodeHostEntity();
        directNodeHostEntity.setIp(host.getIp());
        directNodeHostEntity.setPort(host.getPort());
        return directNodeHostEntity;
    }

    @Override
    public String toString() {
        return "ip=" + getIp() + ", port=" + getPort();
    }

    @Override
    public int hashCode() {
        return getIp().hashCode() + getPort().hashCode();
    }

    @Override
    public Long getRegisterTs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getRefreshTs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getMethodStrs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getClassName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRegisterTs(Long registerTs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRefreshTs(Long refreshTs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMethodStrs(List<String> methodStrs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setClassName(String className) {
        throw new UnsupportedOperationException();
    }
}
