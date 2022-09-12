package com.custom.orm.metadata;

import java.lang.reflect.Field;
import java.util.Set;

public interface ForeignKeyMetaData extends PrimaryKeyMetaData{

    boolean hasForeignKey(Class<?> entityClass);

    boolean isForeignKey(Field field);

    Set<String> getForeignKeyColumnNames(Class<?> entityClass);

    Set<Field> getForeignKeyColumns(Class<?> entityClass);

    Set<String> getForeignKeyNames(Class<?> entityClass);

    String getForeignKeyName(Class<?> entityClass, Field field);

    Set<String> getForeignKeyReferenceClassNames(Class<?> entityClass);

    String getForeignKeyReferenceClassName(Field field);

    Set<Class<?>> getForeignKeyReferenceClasses(Class<?> entityClass);

    <T> Class getForeignKeyReferenceClass(Field field);

    //todo: delete
    Set<String> getForeignKeyReferenceColumnNames(Class<?> entityClass);

    String getForeignKeyReferenceColumnName(Field field);
}
