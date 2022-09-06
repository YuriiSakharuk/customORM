package com.custom.orm.mapper;

import com.custom.orm.annotations.relations.JoinColumn;
import com.custom.orm.annotations.relations.OneToOne;
import com.custom.orm.metadata.MetaDataManager;
import com.custom.orm.metadata.MetaDataManagerImpl;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class EntitiesMapperImpl implements EntitiesMapper {

    private static final MetaDataManager metaDataManager = new MetaDataManagerImpl();

    /**
     * This method returns SQL-query, that specifies JOIN-action for find-methods (this query should be attached to the
     * SELECT-query). It should form a row of JOIN-queries corresponding to the entity mapping (currently, it works,
     * if entity has any number of parent-mappings (so entity may have any number of @OneToOne(mappedBy),
     *
     * @OneToMany(mappedBy) or @ManyToOne(mappedBy)), and/or any number of child-mapping. It does not work properly with
     * composed primary keys, but this bug will be fixed soon.
     * Please, note that it does not work with @ManyToMany.
     * Please, note that in the upcoming changes this method will work only if parent-entity has CascadeType "ALL" or "GET"
     */

    @Override
    public <T> String getFindQuery(Class<T> entityClass) {
        String sql = "SELECT %s FROM %s %s";

        return String.format(
                sql,
                getFieldsForSelect(entityClass),
                metaDataManager.getTableName(entityClass),
                getJoinScript(entityClass)
        );
    }

    private <T> String getJoinScript(Class<T> entityClass) {
        String sql;

        Set<String> oneToOneForeignKeyClassNames = metaDataManager.getOneToOneForeignKeyClassNames(entityClass);
        Set<String> oneToManyForeignKeyClassNames = metaDataManager.getOneToManyForeignKeyClassNames(entityClass);
        Set<String> manyToOneForeignKeyClassNames = metaDataManager.getManyToOneForeignKeyClassNames(entityClass);

        try {
            sql = getParentFindQuery(oneToOneForeignKeyClassNames, entityClass)
                    + getParentFindQuery(oneToManyForeignKeyClassNames, entityClass)
                    + getParentFindQuery(manyToOneForeignKeyClassNames, entityClass);
            if (metaDataManager.hasForeignKey(entityClass))
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
            String foreignKeyClassName = metaDataManager.getTableNameWithoutSchema(foreignKeyClass);

            result.append(String.format("LEFT JOIN %s ON %s = %s ",
                    metaDataManager.getTableNameWithoutSchema(foreignKeyClass),
                    metaDataManager.getTableNameWithoutSchema(entityClass)
                            + "." + metaDataManager.getIdColumnName(entityClass),
                    foreignKeyClassName + "." + metaDataManager.getForeignKeyColumns(foreignKeyClass)
                            .stream()
                            .filter(field -> field.getType().isAssignableFrom(entityClass))
                            .map(metaDataManager::getColumnName)
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

        for (Field field : metaDataManager.getForeignKeyColumns(entityClass)) {
            Class referenceClass = metaDataManager.getForeignKeyReferenceClass(field);
            String referenceClassName = metaDataManager.getForeignKeyReferenceClassName(field);

            result.append(String.format(sql, referenceClassName,
                    referenceClassName + "." + metaDataManager.getIdColumnName(referenceClass),
                    metaDataManager.getTableNameWithoutSchema(entityClass) + "."
                            + metaDataManager.getColumnName(field)));
        }
        result.delete(result.length() - 1, result.length());

        return result.toString();
    }

    @Override
    public <T> String getFieldsForSelect(Class<T> entityClass, Class... entityClassesToAvoid) {
        StringBuilder result = new StringBuilder();

        Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(JoinColumn.class))
                .filter(field -> !field.isAnnotationPresent(OneToOne.class))
                .forEach(field -> result
                                    .append(metaDataManager.getTableNameWithoutSchema(entityClass))
                                    .append(".")
                                    .append(metaDataManager.getColumnName(field))
                                    .append(" AS ")
                                    .append(getTableColumnName(entityClass, field))
                                    .append(", "));

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

    @Override
    public <T> String getTableColumnName(Class<T> entityClass, Field field) {
        return metaDataManager.getTableNameWithoutSchema(entityClass)
                + "_"
                + metaDataManager.getColumnName(field);
    }
}
