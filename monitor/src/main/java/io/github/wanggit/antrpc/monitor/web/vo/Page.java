package io.github.wanggit.antrpc.monitor.web.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Page<T> implements Serializable {

    private static final long serialVersionUID = 6481527263659226197L;

    public Page() {}

    public Page(Integer page, Integer size, Integer count, List<T> items) {
        this.page = page;
        this.size = size;
        this.count = count;
        this.items = items;
    }

    private Integer page;

    private Integer size;

    private Integer count;

    private List<T> items;
}
