package io.github.wanggit.antrpc.commons.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/** Expose the Service object as an RPC Service */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RpcService {}
