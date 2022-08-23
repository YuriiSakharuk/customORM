package com.custom.orm.metadata;

import com.custom.orm.annotations.Column;
import com.custom.orm.annotations.Id;
import com.custom.orm.annotations.Table;
import com.custom.orm.enums.FieldType;

import java.lang.reflect.Field;
import java.util.Arrays;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

public class MetaDataManagerImpl implements MetaDataManager {

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
    public <T> String getIdColumnValues(T object) throws IllegalAccessException {
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

    /**
     * This method returns the name of the given column.
     * It returns either name specified in @Column or the field's name itself.
     */
    @Override
    public String getColumnName(Field field) {
        return ofNullable(field.getAnnotation(Column.class))
                .map(Column::name)
                .filter(name -> name.length() > 0)
                .orElse(field.getName());
    }

    /**
     * This method returns the name of the table corresponding to the entity of the given Class.
     * It returns either name specified in @Table or the entity's name itself.
     */
    @Override
    public <T> String getTableNameWithoutSchema(Class<T> object) {
        return ofNullable(object.getAnnotation(Table.class))
                .map(tableAnnotation -> tableAnnotation.name())
                .filter(name -> name.length() > 0)
                .orElse(object.getSimpleName().toLowerCase());
    }

    /**
     * This method maps Java-types into SQL-types. It works with the limited (most vital) Java-types.
     * You may specify necessary SQL-type in @Column. Otherwise, it maps Java-types by switch-operator.
     * Please, note that if neither of switch-cases succeed, it presumes that field-type is another entity,
     * therefore it will be mapped into "BIGINT", so in database it will be used as id of that other entity
     * (if necessary - as foreign key). So be careful while using type that is not specified in switch-operator.
     * Please, note that field annotated with @Id are always mapped into "SERIAL", so it will be autoincrementing.
     */
    @Override
    public String getColumnType(Field field) {
        if (field.isAnnotationPresent(Column.class)
                && !(field.getAnnotation(Column.class).type().equals(FieldType.DEFAULT)))
            return field.getAnnotation(Column.class).type().toString();

        if (field.isAnnotationPresent(Id.class))
            return "SERIAL";

        String fieldType = field.getType().getSimpleName();

        switch (fieldType) {
            case ("Long"):
                return "BIGINT";
            case ("Integer"):
                return "INTEGER";
            case ("String"):
                return "VARCHAR";
            case ("LocalDateTime"):
                return "DATETIME";
            case ("LocalDate"):
                return "DATE";
            case ("LocalTime"):
                return "TIME";
            case ("Boolean"):
                return "BOOLEAN";
        }
        return "BIGINT";
    }
}
