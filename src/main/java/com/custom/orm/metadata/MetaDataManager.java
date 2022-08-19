package com.custom.orm.metadata;

public interface MetaDataManager {

    <T> String getTableName(Class<T> object);

    <T> String getIdColumnName(Class<T> object);

    <T> String getIdColumnValues(T object) throws IllegalAccessException;

    <T> String getColumnValues(T object);

    <T> String getColumnNames(T object);
}
