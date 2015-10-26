package com.whitebye.redbye.annotation;

import com.whitebye.redbye.core.ForwardType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Administrator on 2015/10/20 0020.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Forward {
    ForwardType type() default  ForwardType.FORWARD;
    String url();
}
