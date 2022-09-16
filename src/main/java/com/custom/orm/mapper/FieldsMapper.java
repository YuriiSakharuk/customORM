package com.custom.orm.mapper;

import com.custom.orm.metadata.implementation.TableMetaDataImpl;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public interface FieldsMapper {

    <T, E> void fillField(Class<T> object, T entity, ResultSet resultSet, Field field, E previousEntity);

    <T> void setGeneratedKeyToObject(T object, PreparedStatement preparedStatement);

    <T> void setValuesFromFields(T object, PreparedStatement preparedStatement);
}
