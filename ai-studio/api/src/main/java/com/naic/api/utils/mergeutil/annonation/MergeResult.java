package com.naic.api.utils.mergeutil.annonation;

import com.naic.api.utils.mergeutil.parser.DefaultMergeResultParser;
import com.naic.api.utils.mergeutil.parser.IMergeResultParser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author fx
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.TYPE})
public @interface MergeResult {
    Class<? extends IMergeResultParser> parser() default DefaultMergeResultParser.class;
}
