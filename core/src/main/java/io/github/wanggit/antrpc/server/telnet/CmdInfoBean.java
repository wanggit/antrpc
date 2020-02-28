package io.github.wanggit.antrpc.server.telnet;

import lombok.Data;

@Data
public class CmdInfoBean {

    private String value;

    private String desc;

    private Class aClass;
}
