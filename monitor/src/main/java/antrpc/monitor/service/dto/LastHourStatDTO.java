package antrpc.monitor.service.dto;

import java.io.Serializable;

public class LastHourStatDTO implements Serializable {

    private static final long serialVersionUID = -6933900049694067255L;
    private Long callCount;
    private Integer avgRt;

    public LastHourStatDTO() {
        this.callCount = 0L;
        this.avgRt = 0;
    }

    public Long getCallCount() {
        return callCount;
    }

    public void setCallCount(Long callCount) {
        this.callCount = callCount;
    }

    public Integer getAvgRt() {
        return avgRt;
    }

    public void setAvgRt(Integer avgRt) {
        this.avgRt = avgRt;
    }
}
