package com.custom.orm.sessions;

import com.custom.orm.annotations.Id;
import com.custom.orm.annotations.Table;
import com.custom.orm.annotations.relations.OneToOne;
import com.custom.orm.mapper.FieldsMapper;
import com.custom.orm.mapper.FieldsMapperImpl;
import com.custom.orm.metadata.MetaDataManager;
import com.custom.orm.metadata.MetaDataManagerImpl;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessionImpl implements Session {

    private Transaction transaction;

    MetaDataManager metaDataManager = new MetaDataManagerImpl();

    FieldsMapper fieldsMapper = new FieldsMapperImpl();

    @Override
    public Transaction beginTransaction() {
        transaction = new Transaction();
        transaction.begin();
        return transaction;
    }

    @SneakyThrows
    @Override
    public <T> T findById(Class<T> object, Long key) {

        String sql = "SELECT * FROM %s WHERE %s = ?";

        Connection connection = transaction.getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement(String.format(
                sql,
                metaDataManager.getTableName(object),
                metaDataManager.getIdColumnName(object)));

        preparedStatement.setLong(1, key);

        T entity = object.getDeclaredConstructor().newInstance();

        ResultSet resultSet = preparedStatement.executeQuery();

        if (!resultSet.next()) {
            return null;
        }

        for (Field field : object.getDeclaredFields()) {
            fieldsMapper.fillFields(object, entity, resultSet, field);
        }
        return entity;
    }

    @SneakyThrows
    @Override
    public <T> List<T> findAll(Class<T> object) {

        String sql = "SELECT * FROM %s";

        Connection connection = transaction.getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement(String.format(sql,
                metaDataManager.getTableName(object)));

        ResultSet resultSet = preparedStatement.executeQuery();

        List<T> result = new ArrayList<>();

        while (resultSet.next()) {
            T entity = object.getDeclaredConstructor().newInstance();
            for (Field field : object.getDeclaredFields()) {
                field.setAccessible(true);
                fieldsMapper.fillFields(object, entity, resultSet, field);
            }
            result.add(entity);
        }
        return result;
    }

    @SneakyThrows
    @Override
    public <T> void create(T object) {
        //checking if such a table exists
        //method from Yura

        String sql = "INSERT INTO %s (%s) VALUES (%s)";

        Connection connection = transaction.getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement(String.format(
                sql,
                metaDataManager.getTableName(object.getClass()),
                metaDataManager.getColumnNames(object),
                metaDataManager.getColumnValues(object)));

        int index = 1;
        for(Field declaredField : object.getClass().getDeclaredFields()){
            declaredField.setAccessible(true);
//            if(declaredField.isAnnotationPresent(OneToOne.class)){
//                // to do
//                continue;
//            }
            preparedStatement.setObject(index++, declaredField.get(object));
        }
        preparedStatement.execute();
    }

    @SneakyThrows
    @Override
    public <T> boolean update(T object) {

        String sql = "UPDATE %s SET %s WHERE id = %s";
        StringBuilder objectIdValue = new StringBuilder();
        StringBuilder columnAndValue = new StringBuilder();

        Field[] fields = object.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);

            Object value = field.get(object);
            if(value==null){
                continue;
            }

            if (field.isAnnotationPresent(Id.class)) {
                objectIdValue.append(value);
            } else {
                    columnAndValue.append(field.getName()).append(" = '").append(value).append("'");
                if (i < fields.length - 1) {
                    columnAndValue.append(",");
                }
            }
        }

        Connection connection = transaction.getConnection();

        return connection.prepareStatement(String.format(
                sql,
                metaDataManager.getTableName(object.getClass()),
                columnAndValue,
                objectIdValue)).execute();
    }

    @SneakyThrows
    @Override
    public <T> boolean delete(T object) {

        String sql = "DELETE FROM %s WHERE id = %s";

        Connection connection = transaction.getConnection();

        return connection.prepareStatement(String.format(sql,
                metaDataManager.getTableName(object.getClass()),
                metaDataManager.getIdColumnValues(object)))
                .execute();
    }

    @Override
    public void close() {
        transaction.close();
    }

    @Override
    public void cancelQuery() {
        transaction.rollback();
    }
}
