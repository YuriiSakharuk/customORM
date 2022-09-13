package com.custom.orm.metadata;

import com.custom.orm.mapper.FieldsMapper;
import com.custom.orm.metadata.implementation.TableMetaDataImpl;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface TableMetaData{

    <T> String getTableName(Class<T> object);

    <T> String getIdColumnName(Class<T> object);

    <T> String getIdColumnValues(T object) throws IllegalAccessException;

    <T> String getPreparedColumnValues(T object);

    <T> String getColumnNames(T object);

    <T> void setObjectsFromFields(T object, PreparedStatement preparedStatement, TableMetaDataImpl metaDataManager, FieldsMapper fieldsMapper);

    <T> List<Field> getDeclaredFields(T object);

    <T> List<Field> getOneToOneDeclaredFields(T object);

    <T> String getTableNameWithoutSchema(Class<T> object);

    <T> boolean checkTableExists(Connection connection, Class<T> entityClass) throws SQLException;
}
