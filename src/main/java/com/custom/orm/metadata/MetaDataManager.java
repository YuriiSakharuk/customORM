package com.custom.orm.metadata;

import java.lang.reflect.Field;
import java.util.Set;

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

    <T> Class getForeignKeyReferenceClass(Class<T> entityClass);

    String getForeignKeyReferenceColumnName(Class<?> entityClass);

    <T> Set<String> getOneToOneForeignKeyClassNames(Class<T> entityClass);

    <T> Set<String> getOneToManyForeignKeyClassNames(Class<T> entityClass);

    <T>Set<String> getManyToOneForeignKeyClassNames(Class<T> entityClass);
}
