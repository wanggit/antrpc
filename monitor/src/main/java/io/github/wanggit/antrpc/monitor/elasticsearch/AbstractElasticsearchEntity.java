package io.github.wanggit.antrpc.monitor.elasticsearch;

public abstract class AbstractElasticsearchEntity {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
