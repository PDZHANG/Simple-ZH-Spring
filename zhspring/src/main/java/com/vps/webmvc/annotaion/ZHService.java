package com.vps.webmvc.annotaion;


import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ZHService {

    String value() default "";
}
