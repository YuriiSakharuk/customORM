package com.custom.orm.metadata;

import java.lang.reflect.Field;

public interface MetaDataManager {

    <T> String getTableName(Class<T> object);

    <T> String getIdColumnName(Class<T> object);

    <T> String getIdColumnValues(T object) throws IllegalAccessException;

    <T> String getColumnValues(T object);

    <T> String getColumnNames(T object);

    String getColumnName(Field field);

    <T> String getTableNameWithoutSchema(Class<T> object);

    String getColumnType(Field field);

    String getPrimaryKeyColumnName(Class<?> entityClass);

    String getComposedPrimaryKeyColumnsNames(Class<?> entityClass);

    boolean hasForeignKey(Class<?> entityClass);

    String getForeignKeyColumnName(Class<?> entityClass);

    String getForeignKeyName(Class<?> entityClass);

    String getForeignKeyReferenceClassName(Class<?> entityClass);

    String getForeignKeyReferenceColumnName(Class<?> entityClass);
}
