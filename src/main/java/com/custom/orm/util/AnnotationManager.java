package com.custom.orm.util;

import com.custom.orm.annotations.Column;
import com.custom.orm.annotations.Id;
import com.custom.orm.annotations.Table;
import com.custom.orm.enums.FieldType;
import com.custom.orm.metadata.MetaDataManager;
import com.custom.orm.metadata.MetaDataManagerImpl;

import java.lang.reflect.Field;
import java.util.Arrays;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

public class AnnotationManager {
    private static MetaDataManager metaDataManager = new MetaDataManagerImpl();

    public static <T> String createTableIfNotExists(T entity) {
        Class<?> entityClass = entity.getClass();
        Table table = entityClass.getAnnotation(Table.class);
        String sql = "CREATE TABLE IF NOT EXISTS %s (%s, %s, %s);";

        if (table == null)
            throw new RuntimeException("Entity \"" + metaDataManager.getTableName(entityClass) + "\" not found!");
        else {
            return String.format(sql, metaDataManager.getTableName(entityClass),
                    getTableNamesTypesConstraints(entityClass),
                    getPrimaryKey(entityClass),
                    getForeignKey(entityClass));
        }
    }

    private static String getForeignKey(Class<?> entityClass) {
        String sql = "CONSTRAINT fk_%s FOREIGN KEY (%s) REFERENCES %s(%s)";

        return sql;
    }

    private static <T> StringBuilder getTableNamesTypesConstraints(Class<T> entityClass) {
        StringBuilder result = new StringBuilder();
        for (Field field : entityClass.getDeclaredFields()) {
            result.append(metaDataManager.getColumnName(field) + " "
                    + getColumnType(field) + " "
                    + getConstraints(field) + ", ");
        }
        result.delete(result.length() - 2, result.length());
        return result;
    }

    private static FieldType getColumnType(Field field) {
        return field.getAnnotation(Column.class).type();
    }

    private static String getPrimaryKey(Class<?> entityClass) {
        String result = "CONSTRAINT pk_%s PRIMARY KEY (%s)";
        return String.format(result,metaDataManager.getTableNameWithoutSchema(entityClass),
                getPrimaryKeyColumns(entityClass));
    }

    private static String getPrimaryKeyColumns(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Column.class))
                .filter(field -> field.getAnnotation(Column.class).primaryKey())
                .map(field -> ofNullable(field.getAnnotation(Column.class))
                        .map(Column::name)
                        .filter(name -> name.length() > 0)
                        .orElse(field.getName()))
                .collect(joining(", "));
    }

    private static StringBuilder getConstraints(Field field) {
        StringBuilder constraintQuery = new StringBuilder();
        Column column = field.getAnnotation(Column.class);

        if (!column.nullable())
            constraintQuery.append("NOT NULL");

        if (column.unique())
            constraintQuery.append("UNIQUE");

        if (field.isAnnotationPresent(Id.class))
            constraintQuery.append("GENERATED ALWAYS AS IDENTITY");

        return constraintQuery;
    }

    public static void main(String[] args) {
        SomeEntity someEntity = new SomeEntity();
        System.out.println(createTableIfNotExists(someEntity));

    }

}
