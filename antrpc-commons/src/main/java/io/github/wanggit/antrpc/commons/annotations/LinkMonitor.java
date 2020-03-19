package io.github.wanggit.antrpc.commons.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface LinkMonitor {}
