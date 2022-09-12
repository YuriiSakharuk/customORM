package com.custom.orm.metadata;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface TableMetaData{

    <T> String getTableName(Class<T> object);

    <T> String getIdColumnName(Class<T> object);

    <T> String getIdColumnValues(T object) throws IllegalAccessException;

    <T> String getColumnValues(T object);

    <T> String getColumnNames(T object);

    <T> List<Field> getDeclaredFields(T object);

    <T> List<Field> getOneToOneDeclaredFields(T object);

    <T> String getTableNameWithoutSchema(Class<T> object);

    <T> boolean tableExists (Connection connection, Class<T> entityClass) throws SQLException;
}
