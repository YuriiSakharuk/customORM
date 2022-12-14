package com.custom.orm.sessions;

import com.custom.orm.enums.CascadeType;
import com.custom.orm.mapper.EntitiesMapper;
import com.custom.orm.mapper.EntitiesMapperImpl;
import com.custom.orm.mapper.FieldsMapper;
import com.custom.orm.mapper.FieldsMapperImpl;
import com.custom.orm.metadata.ColumnMetaData;
import com.custom.orm.metadata.DeclaredFieldsMetaData;
import com.custom.orm.metadata.TableMetaData;
import com.custom.orm.metadata.implementation.ColumnMetaDataImpl;
import com.custom.orm.metadata.implementation.DeclaredFieldsMetaDataImpl;
import com.custom.orm.metadata.implementation.TableMetaDataImpl;
import com.custom.orm.util.TableCreator;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessionImpl implements Session {

    private Transaction transaction;

    private final TableMetaData tableMetaData = new TableMetaDataImpl();
    private final FieldsMapper fieldsMapper = new FieldsMapperImpl();
    private final TableCreator tableCreator = new TableCreator();
    private final EntitiesMapper entitiesMapper = new EntitiesMapperImpl();
    private final ColumnMetaData columnMetaData = new ColumnMetaDataImpl();
    private final DeclaredFieldsMetaData declaredFieldsMetaData = new DeclaredFieldsMetaDataImpl();

    private static final String FIND_BY_ID_SQL_QUERY = "%s WHERE %s = ?;";
    private static final String CREATE_SQL_QUERY = "INSERT INTO %s (%s) VALUES (%s)";
    private static final String UPDATE_SQL_QUERY = "UPDATE %s SET %s WHERE id = %s";
    private static final String DELETE_SQL_QUERY = "DELETE FROM %s WHERE id = %s";

    @Override
    public Transaction beginTransaction() {
        transaction = new Transaction();
        transaction.begin();
        return transaction;
    }

    /**
     * This method returns an object by key (id) from the database.
     *
     * @param object type of the class is passed, which is analogous to the table in the database.
     * @param key    key that will be used to search for a record in the database.
     * @return new instance of the class with the fields filled with the data returned from the database.
     */
    @SneakyThrows
    @Override
    public <T> T findById(Class<T> object, Long key) {

        Connection connection = transaction.getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement(String.format(
                FIND_BY_ID_SQL_QUERY,
                entitiesMapper.getFindQuery(object),
                tableMetaData.getTableNameWithoutSchema(object) + "." + "id"));

        preparedStatement.setLong(1, key);

        T entity = object.getDeclaredConstructor().newInstance();

        ResultSet resultSet = preparedStatement.executeQuery();

        if (!resultSet.next()) {
            return null;
        }

        for (Field field : object.getDeclaredFields()) {
            fieldsMapper.fillField(object, entity, resultSet, field, null);
        }
        return entity;
    }

    /**
     * This method returns a List of all objects from the database,
     * according to the instance of the class that is passed as a parameter to the method.
     *
     * @param object type of the class is passed, which is analogous to the table in the database.
     * @return list of all objects from the database table that were returned in response to the SQL request.
     */
    @SneakyThrows
    @Override
    public <T> List<T> findAll(Class<T> object) {

        String sql = entitiesMapper.getFindQuery(object);

        Connection connection = transaction.getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement(String.format(sql,
                tableMetaData.getTableName(object)));

        ResultSet resultSet = preparedStatement.executeQuery();

        List<T> result = new ArrayList<>();

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

    /**
     * This method adds the object to the database.
     *
     * @param object java object of the application, which should be recorded in the corresponding table in the database.
     * @Id (key) of the object is generated by the database and after recording the object in the database,
     * the key is returned and assigned to the object.
     * Fields marked with the annotation @OneToOne are not entered into the database,
     * they indicate the relationship of objects on the side of the Java application.
     * If the CascadeType value "ALL" or "ADD" is present in the @OneToOne annotation,
     * then when adding an object to the database, an object that has a relationship with our object will also be added.
     */
    @SneakyThrows
    @Override
    public <T> boolean create(T object) {

        Connection connection = transaction.getConnection();

        if (!tableCreator.checkTableExists(connection, object.getClass())) {
            connection.createStatement().execute(tableCreator.createTableIfNotExists(object));
        }

        PreparedStatement preparedStatement = connection.prepareStatement(String.format(
                CREATE_SQL_QUERY,
                tableMetaData.getTableName(object.getClass()),
                columnMetaData.getColumnNames(object),
                columnMetaData.getOperatorsFromFields(object)), Statement.RETURN_GENERATED_KEYS);
        fieldsMapper.setValuesFromFields(object, preparedStatement);

        fieldsMapper.setGeneratedKeyToObject(object, preparedStatement);

        declaredFieldsMetaData.getObjectsFromFieldsOneToOne(object, CascadeType.ALL, CascadeType.ADD).forEach(this::create);

        return true;
    }

    /**
     * This method updates the record in the database according to the object that is passed to the method as a parameter.
     *
     * @param object java application object that should update a record in a database table.
     */
    @SneakyThrows
    @Override
    public <T> void update(T object) {

        StringBuilder columnAndValue = new StringBuilder();

        Connection connection = transaction.getConnection();

        List<Field> declaredFieldsForUpdate = declaredFieldsMetaData.getDeclaredFieldsForUpdate(object);

        for (int i = 0; i < declaredFieldsForUpdate.size(); i++) {
            Field field = declaredFieldsForUpdate.get(i);
            field.setAccessible(true);
            columnAndValue.append(field.getName()).append(" = '").append(field.get(object)).append("'");
            if (i < declaredFieldsForUpdate.size() - 1) {
                columnAndValue.append(",");
            }
        }

        connection.prepareStatement(String.format(
                        UPDATE_SQL_QUERY,
                        tableMetaData.getTableName(object.getClass()),
                        columnAndValue,
                        columnMetaData.getIdColumnValues(object))).execute();
    }

    /**
     * This method deletes an object from the database.
     * If the CascadeType value "ALL" or "REMOVE" is present in the @OneToOne annotation,
     * then when delete an object from the database, an object that has a relationship with our object will also be deleted.
     *
     * @param object java application object that should delete a record in a database table.
     */
    @SneakyThrows
    @Override
    public <T> void delete(T object) {

        Connection connection = transaction.getConnection();

        declaredFieldsMetaData.getObjectsFromFieldsOneToOne(object, CascadeType.ALL, CascadeType.REMOVE).forEach(this::delete);

        connection.prepareStatement(String.format(
                        DELETE_SQL_QUERY,
                        tableMetaData.getTableName(object.getClass()),
                        columnMetaData.getIdColumnValues(object))).execute();
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
