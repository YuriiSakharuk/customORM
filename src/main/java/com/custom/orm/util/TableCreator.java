package com.custom.orm.util;

import com.custom.orm.annotations.Column;
import com.custom.orm.annotations.ComposedPrimaryKey;
import com.custom.orm.annotations.Entity;
import com.custom.orm.annotations.relations.JoinColumn;
import com.custom.orm.annotations.relations.ManyToOne;
import com.custom.orm.annotations.relations.OneToMany;
import com.custom.orm.annotations.relations.OneToOne;
import com.custom.orm.metadata.MetaDataManager;
import com.custom.orm.metadata.implementation.MetaDataManagerImpl;
import org.apache.commons.lang3.StringUtils;
import java.lang.reflect.Field;
import java.util.Arrays;

public class TableCreator {

    private final String EMPTY_LINE = StringUtils.EMPTY;
    private final String NOT_NULL = "NOTNULL";
    private final String UNIQUE = "UNIQUE";

    private final MetaDataManager metaDataManager = new MetaDataManagerImpl();

g    /**
     * This method returns SQL-query that is creating table in database with proper constraints,
     * primary key and foreign key. It composes SQL-query, produced by other methods.
     */
    public <T> String createTableIfNotExists(T entity) {
        Class<?> entityClass = entity.getClass();
        String sql = "CREATE TABLE IF NOT EXISTS %s (%s%s, %s);";
        StringBuilder result = new StringBuilder();

        if (!entityClass.isAnnotationPresent(Entity.class))
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
     * Please, note that currently it is not working with composed primary keys. It will be fixed in upcoming changes.
     */
    private String getForeignKeyQuery(Class<?> entityClass) {
        if (!metaDataManager.hasForeignKey(entityClass))
            return EMPTY_LINE;

        StringBuilder result = new StringBuilder();
        String sql = "CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s(%s)";

        for (Field field : metaDataManager.getForeignKeyColumns(entityClass)) {
            result.append(String.format(sql, metaDataManager.getForeignKeyName(entityClass, field),
                    metaDataManager.getColumnName(field),
                    metaDataManager.getForeignKeyReferenceClassName(field),
                    metaDataManager.getForeignKeyReferenceColumnName(field)));
            result.append(", ");
        }

        trimGetForeignKeyQuery(result);

        return result.toString();
    }

    /**
     * This method returns part of SQL-query that specifies names, types and constraints
     * (except primary key and foreign key) of the table's columns.
     */
    private <T> StringBuilder getTableNamesTypesConstraintsQuery(Class<T> entityClass) {
        StringBuilder result = new StringBuilder();
        for (Field field : entityClass.getDeclaredFields()) {
            if ((field.isAnnotationPresent(OneToOne.class) ||
                    field.isAnnotationPresent(OneToMany.class) ||
                    field.isAnnotationPresent(ManyToOne.class)) &&
                    !field.isAnnotationPresent(JoinColumn.class))
                continue;

            result.append(String.format("%s %s %s, ", metaDataManager.getColumnName(field),
                    metaDataManager.getColumnType(field),
                    getConstraints(field)));

            if (getConstraints(field).isEmpty())
                trimGetTableNamesTypesConstraintsQuery(result);
        }
        return result;
    }

    /**
     * This method returns part of the SQL-query, that specifies primary key of the entity.
     */
    private String getPrimaryKeyQuery(Class<?> entityClass) {
        String result = "CONSTRAINT pk_%s PRIMARY KEY (%s)";

        if (Arrays.stream(entityClass.getDeclaredFields())
                .anyMatch(field -> field.isAnnotationPresent(ComposedPrimaryKey.class)))
            return String.format(result, metaDataManager.getTableNameWithoutSchema(entityClass),
                    metaDataManager.getComposedPrimaryKeyColumnsNames(entityClass));

        return String.format(result, metaDataManager.getTableNameWithoutSchema(entityClass),
                metaDataManager.getPrimaryKeyColumnName(entityClass));
    }

    /**
     * This method returns parts of SQL-query, that specifies constraints for each column. You can specify necessary
     * constraints in @Column.
     */
    private String getConstraints(Field field) {
        if (!field.isAnnotationPresent(Column.class))
            return EMPTY_LINE;

        StringBuilder constraintQuery = new StringBuilder();
        Column column = field.getAnnotation(Column.class);

        if (!column.nullable())
            constraintQuery.append(NOT_NULL);

        if (column.unique())
            constraintQuery.append(UNIQUE);

        return constraintQuery.toString();
    }

    private void trimGetForeignKeyQuery (StringBuilder result){
        if (result.length() > 1)
            result.delete(result.length() - 2, result.length());
    }

    private void trimGetTableNamesTypesConstraintsQuery (StringBuilder result){
        result.delete(result.length() - 3, result.length() - 2);
    }
}
