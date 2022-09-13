package com.custom.orm.mapper;

import com.custom.orm.annotations.relations.JoinColumn;
import com.custom.orm.annotations.relations.OneToOne;
import com.custom.orm.metadata.ColumnMetaData;
import com.custom.orm.metadata.ForeignKeyMetaData;
import com.custom.orm.metadata.MappingMetaData;
import com.custom.orm.metadata.TableMetaData;
import com.custom.orm.metadata.implementation.ColumnMetaDataImpl;
import com.custom.orm.metadata.implementation.ForeignKeyMetaDataImpl;
import com.custom.orm.metadata.implementation.MappingMetaDataImpl;
import com.custom.orm.metadata.implementation.TableMetaDataImpl;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class EntitiesMapperImpl implements EntitiesMapper {

    private final TableMetaData tableMetaData = new TableMetaDataImpl();

    private final ColumnMetaData columnMetaData = new ColumnMetaDataImpl();

    private final ForeignKeyMetaData fkMetaData = new ForeignKeyMetaDataImpl();

    private final MappingMetaData mappingMetaData = new MappingMetaDataImpl();

    /**
     * This method returns SQL-query, that specifies JOIN-action for find-methods (this query should be attached to the
     * SELECT-query). It should form a row of JOIN-queries corresponding to the entity mapping (currently, it works,
     * if entity has any number of parent-mappings (so entity may have any number of @OneToOne(mappedBy),
     *
     * @OneToMany(mappedBy) or @ManyToOne(mappedBy)), and/or any number of child-mapping.
     * Please, note that it does not work with @ManyToMany and @OneToMany.
     * Please, note that in the upcoming changes this method will work only if parent-entity has CascadeType "ALL" or "GET"
     */
    @Override
    public <T> String getFindQuery(Class<T> entityClass) {
        String sql = "SELECT %s FROM %s %s";

        return String.format(
                sql,
                getFieldsForSelect(entityClass),
                tableMetaData.getTableName(entityClass),
                getJoinScript(entityClass)
        );
    }

    /*
     * Returns JOIN part of SQL query
     * */
    private <T> String getJoinScript(Class<T> entityClass) {
        String sql;

        Set<String> oneToOneForeignKeyClassNames = mappingMetaData.getOneToOneForeignKeyClassNames(entityClass);
        Set<String> oneToManyForeignKeyClassNames = mappingMetaData.getOneToManyForeignKeyClassNames(entityClass);
        Set<String> manyToOneForeignKeyClassNames = mappingMetaData.getManyToOneForeignKeyClassNames(entityClass);

        try {
            sql = getParentFindQuery(oneToOneForeignKeyClassNames, entityClass)
                    + getParentFindQuery(oneToManyForeignKeyClassNames, entityClass)
                    + getParentFindQuery(manyToOneForeignKeyClassNames, entityClass);
            if (fkMetaData.hasForeignKey(entityClass))
                sql += getChildFindQuery(entityClass);

            return sql;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not Found");
        }
    }

    /**
     * This method returns JOIN-query for parent-mappings (so for @OneToOne(mappedBy), @OneToMany(mappedBy)
     * or @ManyToOne(mappedBy))  /
     */
    private <T> String getParentFindQuery(Set<String> classNames, Class<T> entityClass) throws
            ClassNotFoundException {
        StringBuilder result = new StringBuilder();

        for (String className : classNames) {
            Class<?> foreignKeyClass = Class.forName(className);
            String foreignKeyClassName = tableMetaData.getTableNameWithoutSchema(foreignKeyClass);

            result.append(String.format("LEFT JOIN %s ON %s = %s ",
                    tableMetaData.getTableNameWithoutSchema(foreignKeyClass),
                    tableMetaData.getTableNameWithoutSchema(entityClass)
                            + "." + tableMetaData.getIdColumnName(entityClass),
                    // separate method
                    foreignKeyClassName + "." + fkMetaData.getForeignKeyColumns(foreignKeyClass)
                            .stream()
                            .filter(field -> field.getType().isAssignableFrom(entityClass))
                            .map(columnMetaData::getColumnName)
                            .collect(Collectors.joining())));
        }
        return result.toString();
    }

    /**
     * This method returns JOIN-query for child-mapping (so for @JoinColumn).
     */
    private <T> String getChildFindQuery(Class<T> entityClass) {
        StringBuilder result = new StringBuilder();
        String sql = "LEFT JOIN %s ON %s = %s ";

        for (Field field : fkMetaData.getForeignKeyColumns(entityClass)) {
            Class referenceClass = fkMetaData.getForeignKeyReferenceClass(field);
            String referenceClassName = fkMetaData.getForeignKeyReferenceClassName(field);

            result.append(String.format(sql, referenceClassName,
                    referenceClassName + "." + tableMetaData.getIdColumnName(referenceClass),
                    tableMetaData.getTableNameWithoutSchema(entityClass) + "."
                            + columnMetaData.getColumnName(field)));
        }
        result.delete(result.length() - 1, result.length());

        return result.toString();
    }

    /*
     * gets all the fields' names for SELECT query
     * */
    @Override
    public <T> String getFieldsForSelect(Class<T> entityClass, Class... entityClassesToAvoid) {
        StringBuilder result = new StringBuilder();

        Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(JoinColumn.class))
                .filter(field -> !field.isAnnotationPresent(OneToOne.class))
                .forEach(field -> result
                        .append(getTableColumnValue(entityClass, field))
                        .append(" AS ")
                        .append(getTableColumnName(entityClass, field))
                        .append(", "));

        // separate method to clean comma
        result.delete(result.length() - 2, result.length());

        Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToOne.class))
                .filter(field -> Arrays
                        .stream(entityClassesToAvoid)
                        .noneMatch(e -> e.equals(field.getType())))
                .forEach(field -> result
                        .append(", ")
                        .append(getFieldsForSelect(field.getType(), entityClass)));

        return result.toString();
    }

    /**
     * Creates String for SQL AS name to parse in mapping in format tableName_columnName
     */
    @Override
    public <T> String getTableColumnName(Class<T> entityClass, Field field) {
        return tableMetaData.getTableNameWithoutSchema(entityClass)
                + "_"
                + columnMetaData.getColumnName(field);
    }

    /**
     * Creates String for SQL AS value to parse in mapping in format tableName.columnName
     */
    private <T> String getTableColumnValue(Class<T> entityClass, Field field) {
        return tableMetaData.getTableNameWithoutSchema(entityClass)
                + "."
                + columnMetaData.getColumnName(field);
    }
}
