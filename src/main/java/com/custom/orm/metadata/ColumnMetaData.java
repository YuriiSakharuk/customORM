package com.custom.orm.metadata;

import java.lang.reflect.Field;

public interface ColumnMetaData {

    String getColumnName(Field field);

    <T> String getColumnNames(T object);

    String getColumnType(Field field);

    <T> String getIdColumnName(Class<T> object);

    <T> String getIdColumnValues(T object) throws IllegalAccessException;

    <T> String getOperatorsFromFields(T object);
}
