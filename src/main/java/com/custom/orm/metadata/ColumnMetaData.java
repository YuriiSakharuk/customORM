package com.custom.orm.metadata;

import java.lang.reflect.Field;

public interface ColumnMetaData {

    String getColumnName(Field field);

    String getColumnType(Field field);
}
