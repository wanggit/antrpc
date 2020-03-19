package io.github.wanggit.antrpc.commons.bean;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@ToString
@Data
public class RpcResponseBean implements Serializable {
    private static final long serialVersionUID = 7269703573557273542L;
    private String id;
    private Long ts;
    private Long reqTs;
    private Object result;
}
