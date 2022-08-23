package com.custom.orm.metadata;

import com.custom.orm.annotations.Column;
import com.custom.orm.annotations.ComposedPrimaryKey;
import com.custom.orm.annotations.Id;
import com.custom.orm.annotations.Table;
import com.custom.orm.annotations.relations.JoinColumn;
import com.custom.orm.enums.FieldType;
import java.lang.reflect.Field;
import java.util.Arrays;

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
                .map(Table::name)
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

    /**
     * This method returns name of the column that is primary key.
     * It works only with primary key that consists of one column.
     */
    @Override
    public String getPrimaryKeyColumnName(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .map(this::getColumnName)
                .collect(joining());
    }

    /**
     * This method returns names of the columns that compose primary key.
     * It works only with primary key that consists of a few columns.
     * Please, note that in order to specify composed primary key, you need to use @ComposedPrimaryKey on each field.
     * Please, use @ComposedPrimaryKey only if your primary key consists of a few column. Never use this annotation for
     * a primary key that consists of only one column.
     */
    public String getComposedPrimaryKeyColumnsNames(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ComposedPrimaryKey.class))
                .map(field -> ofNullable(field.getAnnotation(Column.class))
                        .map(Column::name)
                        .filter(name -> name.length() > 0)
                        .orElse(field.getName()))
                .collect(joining(", "));
    }

    /**
     * This method checks if given entity contains foreign key.
     */
    public boolean hasForeignKey(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .noneMatch(field -> field.isAnnotationPresent(JoinColumn.class));
    }

    /**
     * This method returns name of the column that is foreign key (in other words, column that contains foreign key).
     */
    public String getForeignKeyColumnName(Class<?> entityClass) {
        if (hasForeignKey(entityClass))
            throw new RuntimeException(
                    "Table" + getTableName(entityClass) + "does not contain foreign key!");

        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .map(field -> ofNullable(field.getAnnotation(Column.class))
                        .map(Column::name)
                        .filter(name -> name.length() > 0)
                        .orElse(field.getName()))
                .collect(joining());
    }

    /**
     * This method returns name of the constraint that specifies foreign key.
     */
    public String getForeignKeyName(Class<?> entityClass) {
        if (hasForeignKey(entityClass))
            throw new RuntimeException(
                    "Table" + getTableName(entityClass) + "does not contain foreign key!");

        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .map(field -> field.getAnnotation(JoinColumn.class).name())
                .collect(joining());
    }

    /**
     * This method returns name of the class that foreign key of the given entity references.
     */
    public String getForeignKeyReferenceClassName(Class<?> entityClass) {
        if (hasForeignKey(entityClass))
            throw new RuntimeException(
                    "Table" + getTableName(entityClass) + "does not contain foreign key!");

        Class<?> foreignKeyClass = Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .findAny().get().getType();

        return getTableNameWithoutSchema(foreignKeyClass);
    }

    /**
     * This method returns name of the column that foreign key of the given entity references.
     */
    public String getForeignKeyReferenceColumnName(Class<?> entityClass) {
        if (hasForeignKey(entityClass))
            throw new RuntimeException(
                    "Table" + getTableName(entityClass) + "does not contain foreign key!");

        Class<?> foreignKeyClass = Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .findAny().get().getType();

        return getIdColumnName(foreignKeyClass);
    }
}