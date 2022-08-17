package com.custom.orm.sessions;

import com.custom.orm.annotations.Column;
import com.custom.orm.annotations.Id;
import com.custom.orm.annotations.Table;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

import static java.util.Optional.*;
import static java.util.stream.Collectors.*;

public class SessionImpl implements Session {

    @Override
    public <T> T findById(Class<T> objectClass, Object key) {
        return null;
    }

    @Override
    public <T> List<T> findAll(Class<T> objectClass) {
        return null;
    }

    @SneakyThrows
    @Override
    public <T> void create(T object) {

        //checking if such a table exists
        //method from Yura

        String sql = """
        insert
        into
        %s
        (%s)
        values
        (%s)
        """;

        String tableName = ofNullable(object.getClass().getAnnotation(Table.class))
                .map(tableAnnotation -> tableAnnotation.schema() + "." + tableAnnotation.name())
                .orElse(object.getClass().getSimpleName().toLowerCase());

        Field[] declaredFields = object.getClass().getDeclaredFields();

        String columnNames = Arrays.stream(declaredFields)
                .map(field -> ofNullable(field.getAnnotation(Column.class))
                        .map(Column::name)
                        .orElse(field.getName()))
                .collect(joining(", "));

        String columnValues = Arrays.stream(declaredFields)
                .map(field -> "?")
                .collect(joining(", "));

        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/testdb", "postgres", "123456");

        PreparedStatement preparedStatement = connection.prepareStatement(sql.formatted(tableName, columnNames, columnValues));

        int index = 1;
        for(Field declaredField : declaredFields){
            declaredField.setAccessible(true);
            preparedStatement.setObject(index++, declaredField.get(object));
        }
        preparedStatement.execute();

        connection.close();
    }

    @Override
    public <T> boolean update(T object) {
        return false;
    }

    @SneakyThrows
    @Override
    public <T> boolean delete(T object) {

        String sql = """
        delete
        from
        %s
        where
        id = %s
        """;

        String tableName = ofNullable(object.getClass()
                .getAnnotation(Table.class))
                .map(tableAnnotation -> tableAnnotation.schema() + "." + tableAnnotation.name())
                .orElse(object.getClass()
                        .getSimpleName()
                        .toLowerCase());

        Field idField = Arrays.stream(object.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("Entity doesn't have ID!"));

        idField.setAccessible(true);

        String idValue = idField.get(object).toString();

        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/testdb", "postgres", "123456");

        return connection.prepareStatement(sql.formatted(tableName, idValue)).execute();
    }
}
