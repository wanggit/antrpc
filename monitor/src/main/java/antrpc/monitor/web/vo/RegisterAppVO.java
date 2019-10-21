package antrpc.monitor.web.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterAppVO implements Serializable {

    private static final long serialVersionUID = -809412475618829440L;
    private String appName;
    private String ip;
    private Integer httpPort;
    private Integer rpcPort;
    private Long refreshTs;
}
