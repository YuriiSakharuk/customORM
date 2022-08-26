package com.custom.orm.metadata;

import com.custom.orm.annotations.Column;
import com.custom.orm.annotations.Id;
import com.custom.orm.annotations.relations.JoinColumn;
import com.custom.orm.annotations.Table;
import com.custom.orm.annotations.relations.OneToOne;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

public class MetaDataManagerImpl implements MetaDataManager {

    @Override
    public <T> String getTableName(Class<T> object) {
        return ofNullable(object.getAnnotation(Table.class))
                .map(tableAnnotation -> tableAnnotation.schema().equals("") ?
                        tableAnnotation.name() : tableAnnotation.schema() + "." + tableAnnotation.name())
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
    public <T> String getIdColumnValues(T object) throws IllegalAccessException {
        Field idField = Arrays.stream(object.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("Entity doesn't have ID!"));

        idField.setAccessible(true);

        return idField.get(object).toString();
    }

    @SneakyThrows
    @Override
    public <T> String getColumnValues(T object) {
        List<Field> declaredFields = Arrays.asList(object.getClass().getDeclaredFields());

        return declaredFields.stream()
                .filter(field -> !field.isAnnotationPresent(Id.class))
                .filter(field -> !(field.isAnnotationPresent(OneToOne.class) && !field.isAnnotationPresent(JoinColumn.class)))
                .map(field -> "?")
                .collect(joining(", "));
    }

    @Override
    public <T> String getColumnNames(T object) {
        Field[] declaredFields = object.getClass().getDeclaredFields();

        List<Field> collect = Arrays.stream(declaredFields).filter(field -> !field.isAnnotationPresent(Id.class))
                .filter(field -> !(field.isAnnotationPresent(OneToOne.class) && !field.isAnnotationPresent(JoinColumn.class)))
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < collect.size(); i++) {
            if (collect.get(i).isAnnotationPresent(Column.class)) {
                sb.append(collect.get(i).getAnnotation(Column.class).name());
            } else if (collect.get(i).isAnnotationPresent(JoinColumn.class)) {
                sb.append(collect.get(i).getAnnotation(JoinColumn.class).name());
            } else {
                sb.append( collect.get(i).getName());
            }
            if (i < collect.size() - 1) {
                sb.append(", ");
            }
        }
        System.out.println(sb.toString());
        return sb.toString();

    }

    /*
    * Get all the declared fields of the object, excluding the fields when they are marked by the @OneToOne annotation
    * but not marked by the @JoinColumn annotation.
    * */
    @Override
    public <T> List<Field> getDeclaredFields(T object) {
        return Arrays.stream(object.getClass().getDeclaredFields())
                .filter(field -> !(field.isAnnotationPresent(OneToOne.class) && !field.isAnnotationPresent(JoinColumn.class)))
                .collect(Collectors.toList());
    }

    /*
    * Get declared fields that are marked with the @OneToOne annotation, but are not marked with the @JoinColumn annotation.
    * */
    @Override
    public <T> List<Field> getOneToOneDeclaredFields(T object) {
        return Arrays.stream(object.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToOne.class))
                .filter(field -> !field.isAnnotationPresent(JoinColumn.class))
                .collect(Collectors.toList());
    }
}