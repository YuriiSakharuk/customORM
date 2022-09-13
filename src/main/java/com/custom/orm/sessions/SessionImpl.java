package com.custom.orm.sessions;

import com.custom.orm.annotations.Id;
import com.custom.orm.annotations.relations.JoinColumn;
import com.custom.orm.annotations.relations.OneToOne;
import com.custom.orm.enums.CascadeType;
import com.custom.orm.mapper.EntitiesMapper;
import com.custom.orm.mapper.EntitiesMapperImpl;
import com.custom.orm.mapper.FieldsMapper;
import com.custom.orm.mapper.FieldsMapperImpl;
import com.custom.orm.metadata.TableMetaData;
import com.custom.orm.metadata.implementation.TableMetaDataImpl;
import com.custom.orm.util.TableCreator;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//todo: Class with final Strings
public class SessionImpl implements Session {

    private Transaction transaction;

    private final TableMetaData tableMetaData = new TableMetaDataImpl();

    FieldsMapper fieldsMapper = new FieldsMapperImpl();

    TableCreator tableCreator = new TableCreator();

    private final EntitiesMapper entitiesMapper = new EntitiesMapperImpl();

    @Override
    public Transaction beginTransaction() {
        transaction = new Transaction();
        transaction.begin();
        return transaction;
    }

    /*
    * This method returns an object by key (id) from the database.
    * Params:   object - type of the class is passed, which is analogous to the table in the database.
    *           key - key that will be used to search for a record in the database.
    * Returns:  a new instance of the class with the fields filled with the data returned from the database.
    * */
    @SneakyThrows
    @Override
    public <T> T findById(Class<T> object, Long key) {

        String sql = "%s WHERE %s = ?;";

        Connection connection = transaction.getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement(String.format(
                sql,
                entitiesMapper.getFindQuery(object),
                tableMetaData.getTableNameWithoutSchema(object) + "." + "id"));

        preparedStatement.setLong(1, key);

        // Create a new instance of the object that was passed in the parameters of the findById method.
        T entity = object.getDeclaredConstructor().newInstance();

        ResultSet resultSet = preparedStatement.executeQuery();

        // Checking if the object we want to retrieve by key from the database has been returned.
        if (!resultSet.next()) {
            return null;
        }

        // Filling the fields a new instance of the object with the data receive from the database.
        for (Field field : object.getDeclaredFields()) {
            fieldsMapper.fillField(object, entity, resultSet, field, null);
        }
        return entity;
    }

    /*
    * This method returns a List of all objects from the database,
    * according to the instance of the class that is passed as a parameter to the method.
    * Params:   object - type of the class is passed, which is analogous to the table in the database.
    * Returns:  List of all objects from the database table that were returned in response to the SQL request.
     * */
    @SneakyThrows
    @Override
    public <T> List<T> findAll(Class<T> object) {

        String sql = entitiesMapper.getFindQuery(object);

        Connection connection = transaction.getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement(String.format(sql,
                tableMetaData.getTableName(object)));

        ResultSet resultSet = preparedStatement.executeQuery();

        List<T> result = new ArrayList<>();

        // If the ResultSet returns data from the database,
        // then a new instance of the object is created, this object is filled with the data and placed in the List collection.
        while (resultSet.next()) {
            T entity = object.getDeclaredConstructor().newInstance();
            for (Field field : object.getDeclaredFields()) {
                field.setAccessible(true);
                fieldsMapper.fillField(object, entity, resultSet, field, null);
            }
            result.add(entity);
        }
        return result;
    }

    /*
    * This method adds the object to the database.
    * @Id (key) of the object is generated by the database and after recording the object in the database,
    * the key is returned and assigned to the object.
    * Fields marked with the annotation @OneToOne are not entered into the database,
    * they indicate the relationship of objects on the side of the Java application.
    * If the CascadeType value "ALL" or "ADD" is present in the @OneToOne annotation,
    * then when adding an object to the database, an object that has a relationship with our object will also be added.
    * Params:  object - Java object of the application, which should be recorded in the corresponding table in the database.
    */
    @SneakyThrows
    @Override
    public <T> boolean create(T object) {

        String sql = "INSERT INTO %s (%s) VALUES (%s)";

        Connection connection = transaction.getConnection();

        // Checking if corresponding table exists in database.
        if (!tableMetaData.tableExists(connection, object.getClass()))
            connection.createStatement().execute(tableCreator.createTableIfNotExists(object));

        PreparedStatement preparedStatement = connection.prepareStatement(String.format(
                sql,
                tableMetaData.getTableName(object.getClass()),
                tableMetaData.getColumnNames(object),
                tableMetaData.getColumnValues(object)), Statement.RETURN_GENERATED_KEYS);

        // Get all the declared fields of the object, excluding the fields when they are marked by the @OneToOne annotation
        // but not marked by the @JoinColumn annotation.
        List<Field> declaredFields = tableMetaData.getDeclaredFields(object);

        // Iterate through the array of the object's declared fields and pass the value of each field to the PreparedStatement.
        for (int i = 1; i < declaredFields.size(); i++) {
            Field field = declaredFields.get(i);
            field.setAccessible(true);

            // If the field has the @JoinColumn annotation, then we use reflection API
            // to get the object contained in this field and get the key (id) from this object.
            // This key is transferred to the PreparedStatement according to the column specified in @JoinColumn.
            if(field.isAnnotationPresent(JoinColumn.class)){
                Object fieldObject = object.getClass().getMethod("get" + fieldsMapper.firstLetterWordToUpperCase(field.getName())).invoke(object);
                Long idFieldObject = (Long) fieldObject.getClass().getMethod("getId").invoke(fieldObject);

                preparedStatement.setObject(i, idFieldObject);
                continue;
            }
            preparedStatement.setObject(i, field.get(object));
        }
        preparedStatement.executeUpdate();

        // Get the key (id) of the object that was added to the database and assign it to this object.
        fieldsMapper.setObjectGeneratedKeys(object, preparedStatement);

        // Get declared fields that are marked with the @OneToOne annotation, but are not marked with the @JoinColumn annotation.
        List<Field> oneToOneFields = tableMetaData.getOneToOneDeclaredFields(object);

        for (Field oneToOneField : oneToOneFields) {
            if(Arrays.asList(oneToOneField.getAnnotation(OneToOne.class).cascade()).contains(CascadeType.ALL) ||
                    Arrays.asList(oneToOneField.getAnnotation(OneToOne.class).cascade()).contains(CascadeType.ADD)){
                Object obj = object.getClass().getMethod("get" + fieldsMapper.firstLetterWordToUpperCase(oneToOneField.getName())).invoke(object);
                if(obj!=null) {
                    this.create(obj);
                }
            }
        }
        return true;
    }

    /*
    * This method updates the record in the database according to the object that is passed to the method as a parameter.
    * Params:  object - Java application object that should update a record in a database table.
    * */
    @SneakyThrows
    @Override
    public <T> boolean update(T object) {

        String sql = "UPDATE %s SET %s WHERE id = %s";
        StringBuilder objectIdValue = new StringBuilder();
        StringBuilder columnAndValue = new StringBuilder();

        List<Field> declaredFields = Arrays.stream(object.getClass().getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(OneToOne.class))
                .collect(Collectors.toList());

        for (int i = 0; i < declaredFields.size(); i++) {
            Field field = declaredFields.get(i);
            field.setAccessible(true);

            Object value = field.get(object);
            if(value==null){
                continue;
            }

            if (field.isAnnotationPresent(Id.class)) {
                objectIdValue.append(value);
            } else {
                    columnAndValue.append(field.getName()).append(" = '").append(value).append("'");
                if (i < declaredFields.size() - 1) {
                    columnAndValue.append(",");
                }
            }
        }

        Connection connection = transaction.getConnection();

        return connection.prepareStatement(String.format(
                sql,
                tableMetaData.getTableName(object.getClass()),
                columnAndValue,
                objectIdValue)).execute();
    }

    /*
     * This method deletes an object from the database.
     * If the CascadeType value "ALL" or "REMOVE" is present in the @OneToOne annotation,
     * then when delete an object from the database, an object that has a relationship with our object will also be deleted.
     * Params:  object - Java application object that should delete a record in a database table.
     * Returns: boolean value that reports whether the record was successfully deleted from the database.
     */
    @SneakyThrows
    @Override
    public <T> boolean delete(T object) {

        String sql = "DELETE FROM %s WHERE id = %s";

        Connection connection = transaction.getConnection();

        // Get declared fields that are marked with the @OneToOne annotation.
        List<Field> oneToOneFields = tableMetaData.getOneToOneDeclaredFields(object);

        // If the CascadeType value is "ALL" or "REMOVE" in the field of the object that is marked with the @OneToOne annotation,
        // then recursively call the delete method for the object that is the value in the field.
        for (Field oneToOneField : oneToOneFields) {
            if(Arrays.asList(oneToOneField.getAnnotation(OneToOne.class).cascade()).contains(CascadeType.ALL) ||
                    Arrays.asList(oneToOneField.getAnnotation(OneToOne.class).cascade()).contains(CascadeType.REMOVE)){
                Object obj = object.getClass().getMethod("get" + fieldsMapper.firstLetterWordToUpperCase(oneToOneField.getName())).invoke(object);
                if(obj!=null) {
                   delete(obj);
                }
            }
        }

        return connection.prepareStatement(String.format(sql,
                tableMetaData.getTableName(object.getClass()),
                tableMetaData.getIdColumnValues(object)))
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
