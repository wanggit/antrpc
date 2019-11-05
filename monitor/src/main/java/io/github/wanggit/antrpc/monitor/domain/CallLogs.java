package io.github.wanggit.antrpc.monitor.domain;

import io.github.wanggit.antrpc.monitor.elasticsearch.AbstractElasticsearchEntity;

import java.io.Serializable;

public class CallLogs extends AbstractElasticsearchEntity implements Serializable {

    private static final long serialVersionUID = 3117636154956326329L;

    private String className;

    private String methodName;

    private String ip;

    private Integer port;

    private Long rt;

    private Long start;

    private Long end;

    private Long threadId;

    private String serialNumber;

    private String errorMessage;

    private String caller;

    private String requestId;

    private String argumentsJson;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Long getRt() {
        return rt;
    }

    public void setRt(Long rt) {
        this.rt = rt;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getArgumentsJson() {
        return argumentsJson;
    }

    public void setArgumentsJson(String argumentsJson) {
        this.argumentsJson = argumentsJson;
    }
}
