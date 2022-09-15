package com.custom.orm.metadata;

public interface PrimaryKeyMetaData {

    String getPrimaryKeyColumnName(Class<?> entityClass);

    String getComposedPrimaryKeyColumnsNames(Class<?> entityClass);
}
