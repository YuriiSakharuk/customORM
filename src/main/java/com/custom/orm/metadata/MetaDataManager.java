package com.custom.orm.metadata;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import java.lang.reflect.Field;
import java.util.Set;

public interface MetaDataManager {

    <T> String getTableName(Class<T> object);

    <T> String getIdColumnName(Class<T> object);

    <T> String getIdColumnValues(T object) throws IllegalAccessException;

    <T> String getPraparedColumnValues(T object);

    <T> String getColumnNames(T object);

    <T> List<Field> getDeclaredFields(T object);

    <T> List<Field> getOneToOneDeclaredFields(T object);

    String getColumnName(Field field);

    <T> String getTableNameWithoutSchema(Class<T> object);

    String getColumnType(Field field);

    String getPrimaryKeyColumnName(Class<?> entityClass);

    String getComposedPrimaryKeyColumnsNames(Class<?> entityClass);

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

    Set<String> getForeignKeyReferenceColumnNames(Class<?> entityClass);

    String getForeignKeyReferenceColumnName(Field field);

    <T> Set<String> getOneToOneForeignKeyClassNames(Class<T> entityClass);

    <T> Set<String> getOneToManyForeignKeyClassNames(Class<T> entityClass);

    <T>Set<String> getManyToOneForeignKeyClassNames(Class<T> entityClass);

    <T> boolean checkTableExists(Connection connection, Class<T> entityClass) throws SQLException;
}
