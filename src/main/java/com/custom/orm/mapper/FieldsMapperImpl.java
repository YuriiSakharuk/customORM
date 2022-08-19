package com.custom.orm.mapper;

import com.custom.orm.annotations.Column;
import com.custom.orm.annotations.Id;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.time.LocalDate;

public class FieldsMapperImpl implements FieldsMapper{

    @Override
    public <T> void fillFields(Class<T> object, T entity, ResultSet resultSet, Field field) {
        if (field.isAnnotationPresent(Id.class)) {
            String columnInfo = field.getName();
            fillField(object, entity, resultSet, field, columnInfo);
        } else if (field.isAnnotationPresent(Column.class)) {
            String columnInfo = field.getAnnotation(Column.class).name();
            fillField(object, entity, resultSet, field, columnInfo);
        } else {
            String columnInfo = field.getName();
            fillField(object, entity, resultSet, field, columnInfo);
        }
    }

    @SneakyThrows
    @Override
    public <T> void fillField(Class<T> object, T entity, ResultSet resultSet, Field field, String columnInfo) {
        String setterName = "set" + ((field.getName().charAt(0) + "").toUpperCase()) + field.getName().substring(1);
        if (field.getType().equals(String.class)) {
            String string = resultSet.getString(columnInfo);
            object.getMethod(setterName, String.class).invoke(entity, string);
        } else if (field.getType().equals(LocalDate.class)) {
            LocalDate date = resultSet.getDate(columnInfo).toLocalDate();
            object.getMethod(setterName, LocalDate.class).invoke(entity, date);
        } else if (field.getType().equals(Long.class)) {
            Long longVal = resultSet.getLong(columnInfo);
            object.getMethod(setterName, Long.class).invoke(entity, longVal);
        } else {
            int diff = resultSet.getInt(columnInfo);
            object.getMethod(setterName, field.getType()).invoke(entity, diff);
        }
    }
}
