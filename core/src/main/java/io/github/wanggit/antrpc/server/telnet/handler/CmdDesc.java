package io.github.wanggit.antrpc.server.telnet.handler;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CmdDesc {
    String value();

    String desc() default "";
}
