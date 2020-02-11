package io.github.wanggit.antrpc.boot;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(AntRpcAutoConfiguration.class)
@Documented
@Inherited
public @interface EnableAntRpc {}
