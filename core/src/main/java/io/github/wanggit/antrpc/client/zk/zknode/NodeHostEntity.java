package io.github.wanggit.antrpc.client.zk.zknode;

import io.github.wanggit.antrpc.client.Host;
import io.github.wanggit.antrpc.client.zk.register.RegisterBean;

import java.util.List;
import java.util.Map;

public class NodeHostEntity extends Host {

    private Long registerTs;
    private Long refreshTs;
    private List<String> methodStrs;
    private Map<String, RegisterBean.RegisterBeanMethod> methodMap;
    private String className;

    public NodeHostEntity() {}

    public NodeHostEntity(String ip, Integer port) {
        super(ip, port);
    }

    public Map<String, RegisterBean.RegisterBeanMethod> getMethodMap() {
        return methodMap;
    }

    public void setMethodMap(Map<String, RegisterBean.RegisterBeanMethod> methodMap) {
        this.methodMap = methodMap;
    }

    public Long getRegisterTs() {
        return registerTs;
    }

    public void setRegisterTs(Long registerTs) {
        this.registerTs = registerTs;
    }

    public Long getRefreshTs() {
        return refreshTs;
    }

    public void setRefreshTs(Long refreshTs) {
        this.refreshTs = refreshTs;
    }

    public List<String> getMethodStrs() {
        return methodStrs;
    }

    public void setMethodStrs(List<String> methodStrs) {
        this.methodStrs = methodStrs;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return "ip="
                + getIp()
                + ", port="
                + getPort()
                + ", className="
                + getClassName()
                + ", registerTs="
                + getRegisterTs()
                + ", refreshTs="
                + getRefreshTs()
                + ", methodStrs="
                + getMethodStrs();
    }
}
