package com.custom.orm.metadata;

import com.custom.orm.enums.CascadeType;

import java.lang.reflect.Field;
import java.util.Set;

public interface MappingMetaData {

    <T> Set<String> getOneToOneForeignKeyClassNames(Class<T> entityClass);

    <T> Set<String> getOneToManyForeignKeyClassNames(Class<T> entityClass);

    <T> Set<String> getManyToOneForeignKeyClassNames(Class<T> entityClass);

    boolean checkCascadeType(Field oneToOneField, CascadeType type1, CascadeType type2);
}
