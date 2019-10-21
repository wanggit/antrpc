package antrpc.commons.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RpcRequestBean implements Serializable {
    private static final long serialVersionUID = 7467207129395241404L;
    private String fullClassName;
    private String methodName;
    private List<String> argumentTypes;
    private Object[] argumentValues;
    private Long ts;
    private String id;
    private String caller;
    private String serialNumber;
    /** only request, not response. */
    private boolean oneway;
}
