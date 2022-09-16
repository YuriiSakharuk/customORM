package com.custom.orm.metadata.implementation;

import com.custom.orm.annotations.Table;
import com.custom.orm.metadata.TableMetaData;
import org.apache.commons.lang3.StringUtils;

import static java.util.Optional.ofNullable;

public class TableMetaDataImpl implements TableMetaData {

    private static final String EMPTY_LINE = StringUtils.EMPTY;
    private static final String DOT = ".";

    /**
     * This method returns the name of the table in the database, which is the analog of the class on the Java application side.
     * If a class is annotated with the @Table annotation, the table name and database schema are taken from this annotation.
     * If the class is not annotated with the @Table annotation, the name of the table is taken from the name of that class.
     * @param object type of the class whose object will be written to the database.
     * @return String with the name of the table in the database.
     */
    @Override
    public <T> String getTableName(Class<T> object) {
        return ofNullable(object.getAnnotation(Table.class))
                .map(tableAnnotation -> tableAnnotation.schema().equals(EMPTY_LINE) ?
                        tableAnnotation.name() : tableAnnotation.schema() + DOT + tableAnnotation.name())
                .orElse(object.getSimpleName().toLowerCase());
    }

    /**
     * This method returns the name of the table corresponding to the entity of the given Class.
     * It returns either name specified in @Table or the entity's name itself.
     * @param object type of the class whose object will be written to the database.
     * @return String with the name of the table in the database.
     */
    @Override
    public <T> String getTableNameWithoutSchema(Class<T> object) {
        return ofNullable(object.getAnnotation(Table.class))
                .map(Table::name)
                .filter(name -> name.length() > 0)
                .orElse(object.getSimpleName().toLowerCase());
    }

}
