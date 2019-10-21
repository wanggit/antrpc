package antrpc.monitor.domain;

import antrpc.monitor.elasticsearch.AbstractElasticsearchEntity;
import antrpc.monitor.elasticsearch.annotations.Document;
import lombok.Data;

import java.io.Serializable;

@Data
@Document(indexName = "jvm")
public class Jvm extends AbstractElasticsearchEntity implements Serializable {
    private static final long serialVersionUID = 7278524630791466797L;

    private String appName;
    private Long ts;
    private String attrName;
    private String attrValue;
}
