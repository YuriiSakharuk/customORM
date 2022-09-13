package com.custom.orm.mapper;

import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class FieldsMapperImpl implements FieldsMapper {

    private final EntitiesMapper entitiesMapper = new EntitiesMapperImpl();

    /*
    * This method writes the data received from the database
    * (according to the type of this data) into the field of the new instance of the object.
    * */
    @SneakyThrows
    @Override
    public <T, E> void fillField(Class<T> entityClass, T entity, ResultSet resultSet, Field field, E previousEntity) {
        String columnName = entitiesMapper.getTableColumnName(entityClass, field);

        // Defines the name of the method in the class that will write data
        // from the database into the field of the instance of this class.
        String setterName = "set" + ((field.getName().charAt(0) + "").toUpperCase()) 
                + field.getName().substring(1);
        String getterName = "get" + ((field.getName().charAt(0) + "").toUpperCase())
                + field.getName().substring(1);


        // According to the type of data that is returned in ResultSet from the database,
        // a class method is called that writes this data into the field of the new instance of the object.
        if (field.getType().equals(String.class)) {
            String string = resultSet.getString(columnName);
            entityClass.getMethod(setterName, String.class).invoke(entity, string);

        } else if (field.getType().equals(LocalDate.class)) {
            LocalDate date = resultSet.getDate(columnName).toLocalDate();
            entityClass.getMethod(setterName, LocalDate.class).invoke(entity, date);

        } else if (field.getType().equals(Long.class)) {
            Long longVal = resultSet.getLong(columnName);
            entityClass.getMethod(setterName, Long.class).invoke(entity, longVal);

        } else if (field.getType().equals(Integer.class)){
            int diff = resultSet.getInt(columnName);
            entityClass.getMethod(setterName, field.getType()).invoke(entity, diff);

        } else if (previousEntity != null
                && field.getType().equals(previousEntity.getClass())) {
            entityClass.getMethod(setterName, previousEntity.getClass()).invoke(entity, previousEntity);

        } else {
            Class childEntityClass = field.getType();

            entityClass.getMethod(setterName, childEntityClass)
                    .invoke(entity, childEntityClass.getConstructor().newInstance());

            for (Field theField: childEntityClass.getDeclaredFields()) {
                fillField(
                        childEntityClass,
                        entityClass.getMethod(getterName).invoke(entity),
                        resultSet,
                        theField,
                        entity);
            }
        }
    }

    /*
    * This method get the key (id) of the object that was added to the database and assign it to this object.
    * */
    @SneakyThrows
    @Override
    public <T> void setObjectGeneratedKeys(T object, PreparedStatement preparedStatement) {
        ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
        if(generatedKeys.next()){
            long id = generatedKeys.getLong("id");
            object.getClass().getMethod("setId", Long.class).invoke(object, id);
        }
    }

    /*
    * This method takes a String that starts with a lowercase letter and returns it with an uppercase letter.
    * */
    @Override
    public String firstLetterWordToUpperCase(String fieldName) {
        if(fieldName == null || fieldName.isEmpty()) return "";
        return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

}
