package com.custom.orm.metadata;

import com.custom.orm.annotations.Column;
import com.custom.orm.annotations.Id;
import com.custom.orm.annotations.Table;
import com.custom.orm.annotations.relations.JoinColumn;
import com.custom.orm.annotations.relations.OneToOne;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

public class TableMetaDataImpl implements TableMetaData{

    /*
     * This method returns the name of the table in the database, which is the analog of the class on the Java application side.
     * If a class is annotated with the @Table annotation, the table name and database schema are taken from this annotation.
     * If the class is not annotated with the @Table annotation, the name of the table is taken from the name of that class.
     * */
    @Override
    public <T> String getTableName(Class<T> object) {
        return ofNullable(object.getAnnotation(Table.class))
                .map(tableAnnotation -> tableAnnotation.schema().equals("") ?
                        tableAnnotation.name() : tableAnnotation.schema() + "." + tableAnnotation.name())
                .orElse(object.getSimpleName().toLowerCase());
    }

    /*
     * This method returns the field name that is the primary key in the database.
     * This field is marked with the @Id annotation.
     * */
    @Override
    public <T> String getIdColumnName(Class<T> object) {
        return Arrays.stream(object.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("Entity doesn't have ID!"))
                .getName();
    }

    /*
     * This method returns the value of the field that is the primary key in the database.
     * This field is marked with the @Id annotation.
     * */
    @SneakyThrows
    @Override
    public <T> String getIdColumnValues(T object) {
        Field idField = Arrays.stream(object.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("Entity doesn't have ID!"));

        idField.setAccessible(true);

        return idField.get(object).toString();
    }

    /*
     * This method returns a string of "?" characters that correspond to the number of object fields that will need
     * to be passed in the SQL request to the PreparedStatement.
     * Fields of the object that are marked with the @Id annotation and the @OneToOne annotation,
     * but at the same time are not marked with the @JoinColumn annotation - these fields are skipped.
     * Object fields marked with the @JoinColumn annotation are taken into account.
     * */
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

    /*
     * This method returns a string with the names of all the object's fields.
     * Fields of the object that are marked with the @Id annotation and the @OneToOne annotation,
     * but at the same time are not marked with the @JoinColumn annotation - these fields are skipped.
     * Object fields marked with the @JoinColumn annotation are taken into account.
     * */
    @Override
    public <T> String getColumnNames(T object) {
        Field[] declaredFields = object.getClass().getDeclaredFields();

        List<Field> collect = Arrays.stream(declaredFields)
                .filter(field -> !field.isAnnotationPresent(Id.class))
                .filter(field -> !(field.isAnnotationPresent(OneToOne.class) && !field.isAnnotationPresent(JoinColumn.class)))
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < collect.size(); i++) {
            if (collect.get(i).isAnnotationPresent(Column.class)) {
                sb.append(collect.get(i).getAnnotation(Column.class).name());
            } else if (collect.get(i).isAnnotationPresent(JoinColumn.class)) {
                sb.append(collect.get(i).getAnnotation(JoinColumn.class).name());
            } else {
                sb.append(collect.get(i).getName());
            }
            if (i < collect.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * This method returns the name of the table corresponding to the entity of the given Class.
     * It returns either name specified in @Table or the entity's name itself.
     */
    @Override
    public <T> String getTableNameWithoutSchema(Class<T> object) {
        return ofNullable(object.getAnnotation(Table.class))
                .map(Table::name)
                .filter(name -> name.length() > 0)
                .orElse(object.getSimpleName().toLowerCase());
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

    public <T> boolean tableExists (Connection connection, Class<T> entityClass) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet resultSet = metaData.getTables(
                null, null, getTableNameWithoutSchema(entityClass), null);

        return resultSet.next();
    }
}
