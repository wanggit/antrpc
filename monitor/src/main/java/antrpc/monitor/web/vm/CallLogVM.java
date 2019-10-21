package antrpc.monitor.web.vm;

import lombok.Data;

@Data
public class CallLogVM {

    private String className;

    private Long startTs;

    private Long endTs;

    private Integer page = 0;

    private Integer size = 20;
}
