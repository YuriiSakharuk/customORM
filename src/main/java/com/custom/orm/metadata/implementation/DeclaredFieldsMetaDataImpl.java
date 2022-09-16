package com.custom.orm.metadata.implementation;

import com.custom.orm.annotations.Id;
import com.custom.orm.annotations.relations.JoinColumn;
import com.custom.orm.annotations.relations.OneToOne;
import com.custom.orm.enums.CascadeType;
import com.custom.orm.metadata.DeclaredFieldsMetaData;
import com.custom.orm.metadata.MappingMetaData;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.custom.orm.util.StringManipulation.*;
import static java.util.stream.Collectors.*;

public class DeclaredFieldsMetaDataImpl implements DeclaredFieldsMetaData {

      MappingMetaData mappingMetaData = new MappingMetaDataImpl();

    /**
     * Get all the declared fields of the object, excluding the fields when they are marked by the @OneToOne annotation
     * but not marked by the @JoinColumn annotation.
     *
     * @param object The object to be written to the database.
     * @return List of object fields to be written to the database.
     */
    @Override
    public <T> List<Field> getDeclaredFields(T object) {
        return Arrays.stream(object.getClass().getDeclaredFields())
                .filter(field -> !(field.isAnnotationPresent(OneToOne.class) && !field.isAnnotationPresent(JoinColumn.class)))
                .collect(toList());
    }

    /**
     * This method returns a List of all object fields to be updated in the database.
     *
     * @param object The object to update to the database.
     * @return List of all object fields to be updated in the database.
     */
    @SneakyThrows
    @Override
    public <T> List<Field> getDeclaredFieldsForUpdate(T object) {

        List<Field> fieldsForUpdate = new ArrayList<>();
        List<Field> declaredFields = getDeclaredFields(object);
        for (Field field : declaredFields) {
            field.setAccessible(true);

            Object value = field.get(object);
            if (value == null || field.isAnnotationPresent(Id.class)) {
                continue;
            }
            fieldsForUpdate.add(field);
        }
        return fieldsForUpdate;
    }

    /**
     * Get declared fields that are marked with the @OneToOne annotation, but are not marked with the @JoinColumn annotation.
     *
     * @param object The object to be written to the database.
     * @return List of object fields marked with the annotation @OneToOne, which should be written to the database.
     */
    @Override
    public <T> List<Field> getOneToOneDeclaredFields(T object) {
        return Arrays.stream(object.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToOne.class))
                .filter(field -> !field.isAnnotationPresent(JoinColumn.class))
                .collect(toList());
    }

    /**
     * This method returns a list of objects derived from the object's fields that are annotated with the @OneToOne annotation.
     *
     * @param object The object to be written to the database.
     * @return List of objects that are obtained from fields.
     */
    @SneakyThrows
    @Override
    public <T> List<Object> getObjectsFromFieldsOneToOne(T object, CascadeType type1, CascadeType type2) {
        List<Field> oneToOneFields = getOneToOneDeclaredFields(object)
                .stream()
                .filter(field -> mappingMetaData.checkCascadeType(field, type1, type2))
                .collect(Collectors.toList());

        List<Object> objectsFromFields = new ArrayList<>();
        Object objectFromField;
        for (Field oneToOneField : oneToOneFields) {
            objectFromField = object.getClass().getMethod("get" + firstLetterStringToUpperCase(oneToOneField.getName())).invoke(object);
            if(objectFromField != null) {
                objectsFromFields.add(objectFromField);
            }
        }
        return objectsFromFields;
    }
}
