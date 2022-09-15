package com.custom.orm.metadata;

import java.lang.reflect.Field;
import java.util.Set;

public interface ForeignKeyMetaData {

    boolean hasForeignKey(Class<?> entityClass);

    boolean isForeignKey(Field field);

    Set<Field> getForeignKeyColumns(Class<?> entityClass);

    String getForeignKeyName(Class<?> entityClass, Field field);

    String getForeignKeyReferenceClassName(Field field);

    <T> Class getForeignKeyReferenceClass(Field field);

    String getForeignKeyReferenceColumnName(Field field);
}
