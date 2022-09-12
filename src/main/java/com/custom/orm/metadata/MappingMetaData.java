package com.custom.orm.metadata;

import java.util.Set;

public interface MappingMetaData extends ForeignKeyMetaData{

    <T> Set<String> getOneToOneForeignKeyClassNames(Class<T> entityClass);

    <T> Set<String> getOneToManyForeignKeyClassNames(Class<T> entityClass);

    <T>Set<String> getManyToOneForeignKeyClassNames(Class<T> entityClass);
}
