package com.custom.orm.annotations.relations;

import com.custom.orm.enums.CascadeType;

public @interface ManyToMany {

    String mappedBy() default "";

    CascadeType[] cascadeType() default {};
}
