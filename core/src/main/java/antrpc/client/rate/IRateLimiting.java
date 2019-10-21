package antrpc.client.rate;

import antrpc.client.zk.register.RegisterBean;

/** 访问频率控制 */
public interface IRateLimiting {

    boolean allowAccess(RegisterBean.RegisterBeanMethod registerBeanMethod);
}
