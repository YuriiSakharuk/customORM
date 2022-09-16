package com.custom.orm.metadata.implementation;

import com.custom.orm.annotations.Column;
import com.custom.orm.annotations.Id;
import com.custom.orm.annotations.relations.JoinColumn;
import com.custom.orm.annotations.relations.OneToOne;
import com.custom.orm.enums.FieldType;
import com.custom.orm.metadata.ColumnMetaData;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

public class ColumnMetaDataImpl implements ColumnMetaData {

    /**
     * This method returns the name of the given column.
     * It returns either name specified in @Column or the field's name itself.
     */
    @Override
    public String getColumnName(Field field) {
        return ofNullable(field.getAnnotation(JoinColumn.class))
                .map(JoinColumn::name)
                .orElse(ofNullable(field.getAnnotation(Column.class))
                        .filter(annotation -> annotation.name().length() > 0)
                        .map(Column::name)
                        .orElse(field.getName()));

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

    /**
     * This method returns the field name that is the primary key in the database.
     * This field is marked with the @Id annotation.
     *
     * @param object type of the class whose object will be written to the database.
     * @return String with a field name.
     */
    @Override
    public <T> String getIdColumnName(Class<T> object) {
        return Arrays.stream(object.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("Entity doesn't have ID!"))
                .getName();
    }

    /**
     * This method returns the value of the field that is the primary key in the database.
     * This field is marked with the @Id annotation.
     *
     * @param object The object to be written to the database.
     * @return String with a value that is the primary key in the database
     */
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

    /**
     * This method returns a string of "?" characters that correspond to the number of object fields that will need
     * to be passed in the SQL request to the PreparedStatement.
     * Fields of the object that are marked with the @Id annotation and the @OneToOne annotation,
     * but at the same time are not marked with the @JoinColumn annotation - these fields are skipped.
     * Object fields marked with the @JoinColumn annotation are taken into account.
     *
     * @param object The object to be written to the database.
     * @return String of operators that correspond to the number of object fields.
     */
    @SneakyThrows
    @Override
    public <T> String getOperatorsFromFields(T object) {
        List<Field> declaredFields = Arrays.asList(object.getClass().getDeclaredFields());

        return declaredFields.stream()
                .filter(field -> !field.isAnnotationPresent(Id.class))
                .filter(field -> !(field.isAnnotationPresent(OneToOne.class) && !field.isAnnotationPresent(JoinColumn.class)))
                .map(field -> "?")
                .collect(joining(", "));
    }

    /**
     * This method returns a string with the names of all the object's fields.
     * Fields of the object that are marked with the @Id annotation and the @OneToOne annotation,
     * but at the same time are not marked with the @JoinColumn annotation - these fields are skipped.
     * Object fields marked with the @JoinColumn annotation are taken into account.
     *
     * @param object The object to be written to the database.
     * @return string with the names of all the object's fields.
     */
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
}
