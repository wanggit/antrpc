package io.github.wanggit.antrpc.commons.annotations;

import java.lang.annotation.*;

/** RPC services are automatically injected into objects, similar to Spring's Autowire */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcAutowired {

    boolean required() default true;
}
