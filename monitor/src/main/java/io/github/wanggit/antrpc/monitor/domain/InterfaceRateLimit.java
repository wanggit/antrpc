package io.github.wanggit.antrpc.monitor.domain;

import java.io.Serializable;

public class InterfaceRateLimit implements Serializable {

    private static final long serialVersionUID = 6907401932901865312L;
    private Long id;

    /** 类全名+方法全名 */
    private String fullName;

    private String ip;

    private boolean enable;

    private Integer limit;

    private Integer durationInSeconds;
}
