package io.github.wanggit.antrpc.commons.generic;

import io.github.wanggit.antrpc.commons.bean.Host;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;

@Slf4j
@Data
public class InvokeDTO implements Serializable {

    private static final long serialVersionUID = -8915580980020559039L;

    private Host host;

    private String interfaceName;

    private String methodName;

    private Object[] argumentValues;

    private List<String> parameterTypeNames;
}
