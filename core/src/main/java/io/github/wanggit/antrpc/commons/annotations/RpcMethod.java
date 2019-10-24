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
     * @return
     */
    boolean rateLimitEnable() default false;

    /**
     * 在 durationInSeconds 内接收最多接受 limit 次请求, durationInSeconds与limit都大于0时才生效
     *
     * @return
     */
    int limit() default 0;

    /**
     * 在 durationInSeconds 内接收最多接受 limit 次请求, durationInSeconds与limit都大于0时才生效
     *
     * @return
     */
    int durationInSeconds() default 0;

    /**
     * true 表示当发生熔断与频控时自动从Spring容器中获取一个标识了 OnRpcFail 注解的实现类来调用 <br>
     * 优先级高于 onFailMethod
     *
     * @return
     */
    /*boolean clientDecideOnFailDefaultObject() default false;*/

    /**
     * 因为熔断与频控限制调用失败时，默认再调用此方法，此方法的参数列表必须与原方法相同
     *
     * @return
     */
    /*String onFailMethod() default "";*/
}
