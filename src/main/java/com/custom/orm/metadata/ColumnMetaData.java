package com.custom.orm.metadata;

import java.lang.reflect.Field;

public interface ColumnMetaData extends TableMetaData{

    String getColumnName(Field field);

    String getColumnType(Field field);
}
