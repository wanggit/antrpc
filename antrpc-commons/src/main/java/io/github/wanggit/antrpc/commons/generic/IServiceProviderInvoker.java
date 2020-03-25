package io.github.wanggit.antrpc.commons.generic;

/** 泛化调用 */
public interface IServiceProviderInvoker {

    Object invoke(InvokeDTO invokeDTO);
}
