package com.custom.orm.annotations.relations;

import com.custom.orm.enums.CascadeType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToMany {

    String mappedBy() default "";

    CascadeType[] cascade() default {};
}
