package io.github.wanggit.antrpc.client.rate;

import io.github.wanggit.antrpc.client.zk.register.RegisterBean;

/** 访问频率控制 */
public interface IRateLimiting {

    boolean allowAccess(RegisterBean.RegisterBeanMethod registerBeanMethod);
}
