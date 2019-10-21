package antrpc.server.invoker;

import antrpc.commons.bean.RpcRequestBean;
import antrpc.commons.bean.RpcResponseBean;

public interface IRpcRequestBeanInvoker {
    RpcResponseBean invoke(RpcRequestBean requestBean);
}
