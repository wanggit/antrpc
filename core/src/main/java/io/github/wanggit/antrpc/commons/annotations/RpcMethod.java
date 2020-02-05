package io.github.wanggit.antrpc.commons.annotations;

import java.lang.annotation.*;

/** Expose the Method as an RPC Method */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcMethod {
    /**
     * 是否启用接口频控
     *
     * @return true if enabled
     */
    boolean rateLimitEnable() default false;

    int limit() default 0;

    int durationInSeconds() default 0;
}
