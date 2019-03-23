package com.vps.webmvc.annotaion;


import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ZHRequestMapping {

    String value() default "";
}
