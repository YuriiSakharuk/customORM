package com.custom.orm.metadata.implementation;

import com.custom.orm.annotations.relations.JoinColumn;
import com.custom.orm.metadata.ForeignKeyMetaData;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ForeignKeyMetaDataImpl extends PrimaryKeyMetaDataImpl implements ForeignKeyMetaData {

    /**
     * This method checks if given entity contains foreign key. It returns true if there is at least one @JoinColumn
     */
    @Override
    public boolean hasForeignKey(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .anyMatch(field -> field.isAnnotationPresent(JoinColumn.class));
    }

    /**
     * This method checks if given field is foreign key. It returns true if it is annotaded with @JoinColumn.
     */
    @Override
    public boolean isForeignKey(Field field) {
        return field.isAnnotationPresent(JoinColumn.class);
    }

    /**
     * This method returns set of all fields of the given entity that are foreign key
     * (in other words, all fields that are annotated with @JoinColumn).
     */
    public Set<Field> getForeignKeyColumns(Class<?> entityClass) {
        if (!hasForeignKey(entityClass))
            throw new RuntimeException(
                    "Table " + getTableName(entityClass) + " does not contain foreign key!");

        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .collect(Collectors.toSet());
    }

    /**
     * This method returns name of the constraint that specifies foreign key of the given field.
     */
    @Override
    public String getForeignKeyName(Class<?> entityClass, Field field) {
        if (!isForeignKey(field))
            throw new RuntimeException(
                    "Column " + getColumnName(field) + " does not contain foreign key!");

        String result = "fk_%s_%s";
        return String.format(result, getForeignKeyReferenceClassName(field),
                getTableNameWithoutSchema(entityClass));
    }

    /**
     * This method returns name of the class that foreign key of the given field references.
     */
    @Override
    public String getForeignKeyReferenceClassName(Field field) {
        if (!isForeignKey(field))
            throw new RuntimeException(
                    "Column " + getColumnName(field) + " does not contain foreign key!");

        return getTableNameWithoutSchema(field.getType());
    }

    /**
     * This method returns the class that foreign key of the given field references.
     */
    @Override
    public <T> Class getForeignKeyReferenceClass(Field field) {
        if (!isForeignKey(field))
            throw new RuntimeException(
                    "Column " + getColumnName(field) + " does not contain foreign key!");

        return field.getType();
    }

    /**
     * This method returns name of the column that foreign key of the given field references (usually it references
     * to the primary key).
     */
    @Override
    public String getForeignKeyReferenceColumnName(Field field) {
        if (!isForeignKey(field))
            throw new RuntimeException(
                    "Column " + getColumnName(field) + " does not contain foreign key!");

        return getIdColumnName(field.getType());
    }
}
