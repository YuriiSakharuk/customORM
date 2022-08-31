package com.custom.orm.annotations.relations;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinColumn {
    /**
     * Specifies the name of the corresponding foreign key column in the table (most common - "fieldName_id")
     */
    String name() default "";

    String referencedColumnName() default "";
}
