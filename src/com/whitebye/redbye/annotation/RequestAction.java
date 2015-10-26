package com.whitebye.redbye.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Administrator on 2015/10/20 0020.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestAction {
    String path() default "/";
}
