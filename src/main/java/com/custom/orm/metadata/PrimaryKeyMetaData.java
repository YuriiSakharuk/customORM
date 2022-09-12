package com.custom.orm.metadata;

public interface PrimaryKeyMetaData extends ColumnMetaData{

    String getPrimaryKeyColumnName(Class<?> entityClass);

    String getComposedPrimaryKeyColumnsNames(Class<?> entityClass);
}
