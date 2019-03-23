package com.vps.webmvc.annotaion;


import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ZHAutowired {

    String value() default "";
}
