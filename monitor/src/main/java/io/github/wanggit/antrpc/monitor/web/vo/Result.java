package io.github.wanggit.antrpc.monitor.web.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<X extends Serializable> {

    private Integer code;

    private String msg;

    private X data;

    public Result(Integer code) {
        this.code = code;
    }

    public Result(X data) {
        this.code = ResultCode.SUCCESS;
        this.data = data;
    }
}
