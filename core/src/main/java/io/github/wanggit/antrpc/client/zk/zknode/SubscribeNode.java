package io.github.wanggit.antrpc.client.zk.zknode;

import lombok.Data;

import java.io.Serializable;

@Data
public class SubscribeNode implements Serializable {

    private static final long serialVersionUID = 2267586619170320163L;
    private String host;

    private String className;
}
