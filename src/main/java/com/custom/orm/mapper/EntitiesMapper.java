package com.custom.orm.mapper;

import java.lang.reflect.Field;
import java.util.Set;

public interface EntitiesMapper {
    <T> String getFindQuery(Class<T> entityClass);

    <T> String getFieldsForSelect(Class<T> entityClass, Class... entityClassesToAvoid);

    <T> String getTableColumnName(Class<T> entityClass, Field field);
}
