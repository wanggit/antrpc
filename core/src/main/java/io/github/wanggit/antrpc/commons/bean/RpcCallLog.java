package io.github.wanggit.antrpc.commons.bean;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@ToString
@Data
public class RpcCallLog implements Serializable {

    private static final long serialVersionUID = 3454659672745900232L;
    private String className;

    private String methodName;

    private String ip;

    private Integer port;

    private Long rt;

    private Long start;

    private Long end;

    private Long threadId;

    /** The sequence number of the request call */
    private String serialNumber;

    private String errorMessage;

    private String caller;

    private String requestId;

    private String argumentsJson;

    private Object[] requestArgs;
}
