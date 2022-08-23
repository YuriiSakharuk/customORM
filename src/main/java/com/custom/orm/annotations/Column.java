package com.custom.orm.annotations;

import com.custom.orm.enums.FieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    String name() default "";

    boolean nullable() default true;

    boolean unique() default false;

    //boolean composedPrimaryKey() default false;

    FieldType type() default FieldType.DEFAULT;
}
