package com.wojiushiwo.annotation;

import java.lang.annotation.*;

/**
 * Created by meng on 2018/7/8.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Service {
    String value() default "";
}
