package antrpc.monitor.domain;

import antrpc.monitor.elasticsearch.AbstractElasticsearchEntity;
import antrpc.monitor.elasticsearch.annotations.Document;
import lombok.Data;

import java.io.Serializable;

@Data
@Document(indexName = "call_logs")
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

    /** The sequence number of the request call */
    private String serialNumber;

    private String errorMessage;

    private String caller;

    private String requestId;

    private String argumentsJson;
}
