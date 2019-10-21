package antrpc.monitor.web.vo;

import java.io.Serializable;

public class StatVO implements Serializable {

    private static final long serialVersionUID = 7348402377294666968L;
    private Integer nodeCount;

    private Integer interfaceCount;

    private Long callInLastHour;

    private Integer avgRt;

    public Integer getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(Integer nodeCount) {
        this.nodeCount = nodeCount;
    }

    public Integer getInterfaceCount() {
        return interfaceCount;
    }

    public void setInterfaceCount(Integer interfaceCount) {
        this.interfaceCount = interfaceCount;
    }

    public Long getCallInLastHour() {
        return callInLastHour;
    }

    public void setCallInLastHour(Long callInLastHour) {
        this.callInLastHour = callInLastHour;
    }

    public Integer getAvgRt() {
        return avgRt;
    }

    public void setAvgRt(Integer avgRt) {
        this.avgRt = avgRt;
    }
}
