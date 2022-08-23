package com.custom.orm.util;

import com.custom.orm.annotations.Column;
import com.custom.orm.annotations.ComposedPrimaryKey;
import com.custom.orm.annotations.Id;
import com.custom.orm.annotations.Table;
import com.custom.orm.annotations.relations.JoinColumn;
import com.custom.orm.entity.SomeEntity;
import com.custom.orm.entity.User;
import com.custom.orm.metadata.MetaDataManager;
import com.custom.orm.metadata.MetaDataManagerImpl;

import java.lang.reflect.Field;
import java.util.Arrays;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

public class TableCreator {
    private final MetaDataManager metaDataManager = new MetaDataManagerImpl();

    /**
     * This method returns SQL-query that is creating table in database with proper constraints,
     * primary key and foreign key. It composes SQL-query, produced by other methods.
     */
    public <T> String createTableIfNotExists(T entity) {
        Class<?> entityClass = entity.getClass();
        Table table = entityClass.getAnnotation(Table.class);
        String sql = "CREATE TABLE IF NOT EXISTS %s (%s%s, %s);";
        StringBuilder result = new StringBuilder();

        if (table == null)
            throw new RuntimeException("Entity \"" + metaDataManager.getTableName(entityClass) + "\" not found!");

        result.append(String.format(sql, metaDataManager.getTableName(entityClass),
                getTableNamesTypesConstraintsQuery(entityClass),
                getPrimaryKeyQuery(entityClass),
                getForeignKeyQuery(entityClass)));

        if (getForeignKeyQuery(entityClass).length() < 1)
            result.delete(result.length() - 4, result.length() - 2);

        return result.toString();
    }

    /**
     * This method returns part of the SQL-query, that specifies foreign key creation. It creates foreign key as a separate
     * constraints and specifies its name. SQL-query will be returned only if entities are properly mapped with @JoinColumn.
     * Otherwise, this method will not affect the basic SQL-query.
     */
    private String getForeignKeyQuery(Class<?> entityClass) {
        if (Arrays.stream(entityClass.getDeclaredFields())
                .noneMatch(field -> field.isAnnotationPresent(JoinColumn.class)))
            return "";

        String sql = "CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s(%s)";

        return String.format(sql, getForeignKeyName(entityClass),
                getForeignKeyColumnName(entityClass),
                getForeignKeyReferenceClassName(entityClass),
                getForeignKeyReferenceColumnName(entityClass));
    }

    /**
     * This method returns name of the column that foreign key of the given entity references.
     */
    public String getForeignKeyReferenceColumnName(Class<?> entityClass) {
        if (hasForeignKey(entityClass))
            throw new RuntimeException(
                    "Table" + metaDataManager.getTableName(entityClass) + "does not contain foreign key!");

        Class<?> foreignKeyClass = Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .findAny().get().getType();

        return metaDataManager.getIdColumnName(foreignKeyClass);
    }

    /**
     * This method returns name of the class that foreign key of the given entity references.
     */
    public String getForeignKeyReferenceClassName(Class<?> entityClass) {
        if (hasForeignKey(entityClass))
            throw new RuntimeException(
                    "Table" + metaDataManager.getTableName(entityClass) + "does not contain foreign key!");

        Class<?> foreignKeyClass = Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .findAny().get().getType();

        return metaDataManager.getTableNameWithoutSchema(foreignKeyClass);
    }

    /**
     * This method returns name of the constraint that specifies foreign key.
     */
    public String getForeignKeyName(Class<?> entityClass) {
        if (hasForeignKey(entityClass))
            throw new RuntimeException(
                    "Table" + metaDataManager.getTableName(entityClass) + "does not contain foreign key!");

        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .map(field -> field.getAnnotation(JoinColumn.class).name())
                .collect(joining());
    }

    /**
     * This method returns name of the column that is foreign key (in other words, column that contains foreign key).
     */
    public String getForeignKeyColumnName(Class<?> entityClass) {
        if (hasForeignKey(entityClass))
            throw new RuntimeException(
                    "Table" + metaDataManager.getTableName(entityClass) + "does not contain foreign key!");

        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .map(field -> ofNullable(field.getAnnotation(Column.class))
                        .map(Column::name)
                        .filter(name -> name.length() > 0)
                        .orElse(field.getName()))
                .collect(joining());
    }

    /**
     * This method checks if given entity contains foreign key.
     */
    public boolean hasForeignKey(Class<?> entityClass) {
       return Arrays.stream(entityClass.getDeclaredFields())
                .noneMatch(field -> field.isAnnotationPresent(JoinColumn.class));
    }

    /**
     * This method returns part of SQL-query that specifies names, types and constraints
     * (except primary key and foreign key) of the table's columns.
     */
    private <T> StringBuilder getTableNamesTypesConstraintsQuery(Class<T> entityClass) {
        StringBuilder result = new StringBuilder();
        for (Field field : entityClass.getDeclaredFields()) {
            result.append(String.format("%s %s %s, ", metaDataManager.getColumnName(field),
                    getColumnType(field),
                    getConstraints(field)));

            if (getConstraints(field).length() < 1)
                result.delete(result.length() - 3, result.length() - 2);
        }
        return result;
    }

    private String getColumnType(Field field) {
        return metaDataManager.getColumnType(field);

    }

    /**
     * This method returns part of the SQL-query, that specifies primary key of the entity.
     */
    private String getPrimaryKeyQuery(Class<?> entityClass) {
        String result = "CONSTRAINT pk_%s PRIMARY KEY (%s)";

        if (Arrays.stream(entityClass.getDeclaredFields())
                .anyMatch(field -> field.isAnnotationPresent(ComposedPrimaryKey.class)))
            return String.format(result, metaDataManager.getTableNameWithoutSchema(entityClass),
                    getComposedPrimaryKeyColumnsNames(entityClass));

        return String.format(result, metaDataManager.getTableNameWithoutSchema(entityClass),
                getPrimaryKeyColumnName(entityClass));
    }

    /**
     * This method returns name of the column that is primary key.
     * It works only with primary key that consists of one column.
     */
    public String getPrimaryKeyColumnName(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .map(metaDataManager::getColumnName)
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
     * This method returns parts of SQL-query, that specifies constraints for each column. You can specify necessary
     * constraints in @Column.
     */
    private String getConstraints(Field field) {
        if (!field.isAnnotationPresent(Column.class))
            return "";

        StringBuilder constraintQuery = new StringBuilder();
        Column column = field.getAnnotation(Column.class);

        if (!column.nullable())
            constraintQuery.append("NOT NULL");

        if (column.unique())
            constraintQuery.append("UNIQUE");

        return constraintQuery.toString();
    }

/*    public static void main(String[] args) {
        SomeEntity someEntity = new SomeEntity();
        User user = new User();
        System.out.println(createTableIfNotExists(someEntity));

    }*/

}
