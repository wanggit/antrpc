package io.github.wanggit.antrpc.commons.bean.error;

import lombok.Data;

import java.io.Serializable;

@Data
public class RpcResponseError implements IError, Serializable {

    private static final long serialVersionUID = 3790991837921547463L;
    private String code;

    private String message;
}
