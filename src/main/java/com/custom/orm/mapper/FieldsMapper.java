package com.custom.orm.mapper;

import java.lang.reflect.Field;
import java.sql.ResultSet;

public interface FieldsMapper {

    <T> void fillFields(Class<T> object, T entity, ResultSet resultSet, Field field);

    <T> void fillField(Class<T> object, T entity, ResultSet resultSet, Field field, String columnInfo);
}