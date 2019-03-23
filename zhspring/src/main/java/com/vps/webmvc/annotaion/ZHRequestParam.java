package com.vps.webmvc.annotaion;


import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ZHRequestParam {

    String value() default "";
}
