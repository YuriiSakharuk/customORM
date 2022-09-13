package com.custom.orm.metadata.implementation;

import com.custom.orm.annotations.Column;
import com.custom.orm.annotations.ComposedPrimaryKey;
import com.custom.orm.annotations.Id;
import com.custom.orm.metadata.PrimaryKeyMetaData;
import java.util.Arrays;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

public class PrimaryKeyMetaDataImpl extends ColumnMetaDataImpl implements PrimaryKeyMetaData {

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
    @Override
    public String getComposedPrimaryKeyColumnsNames(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ComposedPrimaryKey.class))
                .map(field -> ofNullable(field.getAnnotation(Column.class))
                        .map(Column::name)
                        .filter(name -> name.length() > 0)
                        .orElse(field.getName()))
                .collect(joining(", "));
    }
}
