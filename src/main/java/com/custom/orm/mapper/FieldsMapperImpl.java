package com.custom.orm.mapper;

import com.custom.orm.annotations.relations.JoinColumn;
import com.custom.orm.metadata.DeclaredFieldsMetaData;
import com.custom.orm.metadata.implementation.DeclaredFieldsMetaDataImpl;
import com.custom.orm.util.StringManipulation;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

public class FieldsMapperImpl implements FieldsMapper {

    private final EntitiesMapper entitiesMapper = new EntitiesMapperImpl();
    private final DeclaredFieldsMetaData declaredFieldsMetaData = new DeclaredFieldsMetaDataImpl();

    /**
     * This method writes the data received from the database
     * (according to the type of this data) into the field of the new instance of the object.
     *
     * @param entityClass Type of the class whose object will be written to the database.
     * @param entity The object to be written to the database.
     * @param field The field of the object in which you want to write the value from the database.
     * @param previousEntity
     */
    @SneakyThrows
    @Override
    public <T, E> void fillField(Class<T> entityClass, T entity, ResultSet resultSet, Field field, E previousEntity) {
        String columnName = entitiesMapper.getTableColumnName(entityClass, field);

        String setterName = "set" + ((field.getName().charAt(0) + "").toUpperCase()) 
                + field.getName().substring(1);
        String getterName = "get" + ((field.getName().charAt(0) + "").toUpperCase())
                + field.getName().substring(1);

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

    /**
     * This method gets the key (id) of the record that was added to the database
     * and assigns it to the object corresponding to that record.
     *
     * @param object The object in which need to write the value of the primary key obtained from the database.
     */
    @SneakyThrows
    @Override
    public <T> void setGeneratedKeyToObject(T object, PreparedStatement preparedStatement) {
        ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
        if(generatedKeys.next()){
            long id = generatedKeys.getLong("id");
            object.getClass().getMethod("setId", Long.class).invoke(object, id);
        }
    }

    /**
     * This method transfers the values from the fields of the object to the database, through PreparedStatement.
     *
     * @param object The object to be written to the database.
     */
    @SneakyThrows
    @Override
    public <T> void setValuesFromFields(T object, PreparedStatement preparedStatement) {
        List<Field> declaredFields = declaredFieldsMetaData.getDeclaredFields(object);

        for (int i = 1; i < declaredFields.size(); i++) {
            Field field = declaredFields.get(i);
            field.setAccessible(true);

            if(field.isAnnotationPresent(JoinColumn.class)){
                Object fieldObject = object.getClass().getMethod("get" + StringManipulation.firstLetterStringToUpperCase(field.getName())).invoke(object);
                Long idFieldObject = (Long) fieldObject.getClass().getMethod("getId").invoke(fieldObject);

                preparedStatement.setObject(i, idFieldObject);
                continue;
            }
            preparedStatement.setObject(i, field.get(object));
        }
        preparedStatement.executeUpdate();
    }
}
