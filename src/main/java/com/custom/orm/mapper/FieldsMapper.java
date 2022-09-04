package com.custom.orm.mapper;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public interface FieldsMapper {

//    <T> void fillFields(Class<T> object, T entity, ResultSet resultSet, Field field);

    <T, E> void fillField(Class<T> object, T entity, ResultSet resultSet, Field field, E previousEntity);

    <T> void setObjectGeneratedKeys(T object, PreparedStatement preparedStatement);

    String firstLetterWordToUpperCase(String fieldName);
}
