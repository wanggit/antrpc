package io.github.wanggit.antrpc.commons.config;

import lombok.Data;

@Data
public class TelnetConfig {

    private Boolean enable = false;

    private Integer port = 7338;

    private String password;
}
