package com.custom.orm.mapper;

import java.util.Set;

public interface EntitiesMapper {
    <T> String getFindQuery(Class<T> entityClass);
}
