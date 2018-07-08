package com.wojiushiwo.annotation;

import java.lang.annotation.*;

/**
 * Created by meng on 2018/7/8.
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
    String value() default "";
}
