package com.whitebye.redbye.annotation;

import com.whitebye.redbye.core.RequestMethod;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Administrator on 2015/10/20 0020.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface HandleMethod {

    RequestMethod method() default RequestMethod.GET;

    String path();
}
