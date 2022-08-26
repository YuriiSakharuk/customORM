package com.custom.orm.metadata;

import java.lang.reflect.Field;
import java.util.List;

public interface MetaDataManager {

    <T> String getTableName(Class<T> object);

    <T> String getIdColumnName(Class<T> object);

    <T> String getIdColumnValues(T object) throws IllegalAccessException;

    <T> String getColumnValues(T object);

    <T> String getColumnNames(T object);

    <T> List<Field> getDeclaredFields(T object);

    <T> List<Field> getOneToOneDeclaredFields(T object);
}
