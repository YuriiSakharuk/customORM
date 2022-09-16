package com.custom.orm.mapper;

import com.custom.orm.annotations.relations.JoinColumn;
import com.custom.orm.annotations.relations.OneToOne;
import com.custom.orm.exceptions.CustomClassNotFoundException;
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

    private static final String FIND_QUERY = "SELECT %s FROM %s %s";
    private static final String JOIN_QUERY = "LEFT JOIN %s ON %s = %s ";
    private static final String DOT = ".";
    private static final String UNDERSCORE = "_";
    private static final String COMMA_AND_SPACE = ", ";
    private static final String AS = " AS ";

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

        return String.format(
                FIND_QUERY,
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
            throw new CustomClassNotFoundException(e);
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

            result.append(String.format(JOIN_QUERY,
                    tableMetaData.getTableNameWithoutSchema(foreignKeyClass),
                    tableMetaData.getTableNameWithoutSchema(entityClass)
                            + DOT + columnMetaData.getIdColumnName(entityClass),
                    // separate method
                    foreignKeyClassName + DOT + fkMetaData.getForeignKeyColumns(foreignKeyClass)
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

        for (Field field : fkMetaData.getForeignKeyColumns(entityClass)) {
            Class referenceClass = fkMetaData.getForeignKeyReferenceClass(field);
            String referenceClassName = fkMetaData.getForeignKeyReferenceClassName(field);

            result.append(String.format(JOIN_QUERY, referenceClassName,
                    referenceClassName + DOT + columnMetaData.getIdColumnName(referenceClass),
                    tableMetaData.getTableNameWithoutSchema(entityClass) + DOT
                            + columnMetaData.getColumnName(field)));
        }
        trimChildFindQuery(result);

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
                        .append(AS)
                        .append(getTableColumnName(entityClass, field))
                        .append(COMMA_AND_SPACE));

        // separate method to clean comma
        trimFieldsForSelect(result);

        Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToOne.class))
                .filter(field -> Arrays
                        .stream(entityClassesToAvoid)
                        .noneMatch(e -> e.equals(field.getType())))
                .forEach(field -> result
                        .append(COMMA_AND_SPACE)
                        .append(getFieldsForSelect(field.getType(), entityClass)));

        return result.toString();
    }

    /**
     * Creates String for SQL AS name to parse in mapping in format tableName_columnName
     */
    @Override
    public <T> String getTableColumnName(Class<T> entityClass, Field field) {
        return tableMetaData.getTableNameWithoutSchema(entityClass)
                + UNDERSCORE
                + columnMetaData.getColumnName(field);
    }

    /**
     * Creates String for SQL AS value to parse in mapping in format tableName.columnName
     */
    private <T> String getTableColumnValue(Class<T> entityClass, Field field) {
        return tableMetaData.getTableNameWithoutSchema(entityClass)
                + DOT
                + columnMetaData.getColumnName(field);
    }

    private void trimChildFindQuery(StringBuilder result) {
        result.delete(result.length() - 1, result.length());
    }

    private void trimFieldsForSelect(StringBuilder result) {
        result.delete(result.length() - 2, result.length());
    }
}
