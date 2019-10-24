package io.github.wanggit.antrpc.server.invoker;

import io.github.wanggit.antrpc.commons.bean.RpcRequestBean;
import io.github.wanggit.antrpc.commons.bean.RpcResponseBean;

public interface IRpcRequestBeanInvoker {
    RpcResponseBean invoke(RpcRequestBean requestBean);
}
