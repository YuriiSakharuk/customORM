package com.custom.orm.mapper;

import com.custom.orm.entity.Profile;
import com.custom.orm.entity.SomeEntity;
import com.custom.orm.entity.User;
import com.custom.orm.metadata.MetaDataManager;
import com.custom.orm.metadata.MetaDataManagerImpl;

import java.util.Set;

public class EntitiesMapperImpl implements EntitiesMapper {

    private static final MetaDataManager metaDataManager = new MetaDataManagerImpl();

    /**
     * This method returns SQL-query, that specifies JOIN-action for find-methods (this query should be attached to the
     * SELECT-query). It should form a row of JOIN-queries corresponding to the entity mapping (currently, it works,
     * if entity has any number of parent-mappings (so entity may have any number of @OneToOne(mappedBy),
     *
     * @OneToMany(mappedBy) or @ManyToOne(mappedBy)), and/or ONLY ONE child-mapping
     * (so entity can have only one @JoinColumn). This bug will be soon fixed.).
     * Please, note that it does not work with @ManyToMany.
     * Please, note that in the upcoming changes this method will work only if parent-entity has CascadeType "ALL" or "GET"
     */
    @Override
    public <T> String getFindQuery(Class<T> entityClass) {
        String sql = "";

        Set<String> oneToOneForeignKeyClassNames = metaDataManager.getOneToOneForeignKeyClassNames(entityClass);
        Set<String> oneToManyForeignKeyClassNames = metaDataManager.getOneToManyForeignKeyClassNames(entityClass);
        Set<String> manyToOneForeignKeyClassNames = metaDataManager.getManyToOneForeignKeyClassNames(entityClass);

        try {
            sql = getParentFindQuery(oneToOneForeignKeyClassNames, entityClass)
                    + getParentFindQuery(oneToManyForeignKeyClassNames, entityClass)
                    + getParentFindQuery(manyToOneForeignKeyClassNames, entityClass);
            if (metaDataManager.hasForeignKey(entityClass))
                sql += getChildFindQuery(entityClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not Found");
        }
        return sql;
    }

    /**
     * This method returns JOIN-query for parent-mappings (so for @OneToOne(mappedBy), @OneToMany(mappedBy)
     * or @ManyToOne(mappedBy))  /
     */
    private  <T> String getParentFindQuery(Set<String> classNames, Class<T> entityClass) throws
            ClassNotFoundException {
        StringBuilder result = new StringBuilder();

        for (String className : classNames) {
            Class<?> foreignKeyClass = Class.forName(className);
            String foreignKeyClassName = metaDataManager.getTableNameWithoutSchema(foreignKeyClass);

            result.append(String.format("LEFT JOIN %s ON %s = %s ",
                    metaDataManager.getTableNameWithoutSchema(foreignKeyClass),
                    metaDataManager.getTableNameWithoutSchema(entityClass)
                            + "." + metaDataManager.getIdColumnName(entityClass),
                    foreignKeyClassName + "." + metaDataManager.getForeignKeyColumnName(foreignKeyClass)));
        }
        return result.toString();
    }

    /**
     * This method returns JOIN-query for child-mapping (so for @JoinColumn).
     * Please, note that currently it is working only, if entity has one @JoinColumn (see the reasons in getFindQuery)
     */
    private  <T> String getChildFindQuery(Class<T> entityClass) {
        StringBuilder result = new StringBuilder();
        String sql = "LEFT JOIN %s ON %s = %s";
        Class referenceClass = metaDataManager.getForeignKeyReferenceClass(entityClass);
        String referenceClassName = metaDataManager.getForeignKeyReferenceClassName(entityClass);

        return String.format(sql, referenceClassName,
                referenceClassName + "." + metaDataManager.getIdColumnName(referenceClass),
                metaDataManager.getTableNameWithoutSchema(entityClass) + "."
                        + metaDataManager.getForeignKeyColumnName(entityClass));
    }

    //For local testing.
    public static void main(String[] args) {
        User user = new User();
        SomeEntity someEntity = new SomeEntity();
        Profile profile = new Profile();

       // System.out.println(getFindQuery(user.getClass()));
    }
}
