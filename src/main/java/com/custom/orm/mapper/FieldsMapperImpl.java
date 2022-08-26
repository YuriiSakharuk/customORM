package com.custom.orm.mapper;

import com.custom.orm.annotations.Column;
import com.custom.orm.annotations.Id;
import com.custom.orm.annotations.relations.OneToOne;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
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
        } else if(field.isAnnotationPresent(OneToOne.class)) {
            boolean q = true;
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

    /*
    * This method get the key (id) of the object that was added to the database and assign it to this object.
    * */
    @SneakyThrows
    @Override
    public <T> void setObjectGeneratedKeys(T object, PreparedStatement preparedStatement) {
        ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
        if(generatedKeys.next()){
            long id = generatedKeys.getLong("id");
            object.getClass().getMethod("setId", Long.class).invoke(object, id);
        }
    }

    /*
    * This method takes a String that starts with a lowercase letter and returns it with an uppercase letter.
    * */
    @Override
    public String firstLetterWordToUpperCase(String fieldName) {
        if(fieldName == null || fieldName.isEmpty()) return "";
        return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }
}
