package com.custom.orm.metadata;

import com.custom.orm.annotations.Column;
import com.custom.orm.annotations.Id;
import com.custom.orm.annotations.Table;

import java.lang.reflect.Field;
import java.util.Arrays;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

public class MetaDataManagerImpl implements MetaDataManager{

    @Override
    public <T> String getTableName(Class<T> object) {
        return ofNullable(object.getAnnotation(Table.class))
                .map(tableAnnotation -> tableAnnotation.schema() + "." + tableAnnotation.name())
                .filter(name -> name.length() > 1)
                .orElse(object.getSimpleName().toLowerCase());
    }

    @Override
    public <T> String getIdColumnName(Class<T> object) {
        return Arrays.stream(object.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("Entity doesn't have ID!"))
                .getName();
    }

    @Override
    public  <T> String getIdColumnValues(T object) throws IllegalAccessException {
        Field idField = Arrays.stream(object.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("Entity doesn't have ID!"));

        idField.setAccessible(true);

        return idField.get(object).toString();
    }

    @Override
    public <T> String getColumnValues(T object) {
        Field[] declaredFields = object.getClass().getDeclaredFields();

        return Arrays.stream(declaredFields)
                .map(field -> "?")
                .collect(joining(", "));
    }

    @Override
    public <T> String getColumnNames(T object) {
        Field[] declaredFields = object.getClass().getDeclaredFields();

        return Arrays.stream(declaredFields)
                .map(field -> ofNullable(field.getAnnotation(Column.class))
                        .map(Column::name)
                        .orElse(field.getName()))
                .collect(joining(", "));
    }

    @Override
    public String getColumnName(Field field){
        return  ofNullable(field.getAnnotation(Column.class))
                .map(Column::name)
                .filter(name -> name.length() > 0)
                .orElse(field.getName());
    }

    @Override
    public <T> String getTableNameWithoutSchema(Class<T> object){
        return ofNullable(object.getAnnotation(Table.class))
                .map(tableAnnotation -> tableAnnotation.name())
                .filter(name -> name.length() > 0)
                .orElse(object.getSimpleName().toLowerCase());
    }
}
