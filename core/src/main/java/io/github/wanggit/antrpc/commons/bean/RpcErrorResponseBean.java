package io.github.wanggit.antrpc.commons.bean;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class RpcErrorResponseBean extends RpcResponseBean {

    private static final long serialVersionUID = 3768405936270999603L;
    private String errorMessage;
}
