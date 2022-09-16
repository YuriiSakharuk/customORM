package com.custom.orm.metadata;

import com.custom.orm.enums.CascadeType;

import java.lang.reflect.Field;
import java.util.List;

public interface DeclaredFieldsMetaData {

    <T> List<Field> getDeclaredFields(T object);

    <T> List<Field> getDeclaredFieldsForUpdate(T object);

    <T> List<Field> getOneToOneDeclaredFields(T object);

   <T> List<Object> getObjectsFromFieldsOneToOne(T object, CascadeType type1, CascadeType type2);
}
