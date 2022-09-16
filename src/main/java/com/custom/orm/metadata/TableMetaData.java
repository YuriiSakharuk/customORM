package com.custom.orm.metadata;

public interface TableMetaData{

    <T> String getTableName(Class<T> object);

    <T> String getTableNameWithoutSchema(Class<T> object);
}
