package com.custom.orm.metadata;

import com.custom.orm.annotations.Column;
import com.custom.orm.annotations.ComposedPrimaryKey;
import com.custom.orm.annotations.Id;
import com.custom.orm.annotations.Table;
import com.custom.orm.annotations.relations.JoinColumn;
import com.custom.orm.annotations.relations.ManyToOne;
import com.custom.orm.annotations.relations.OneToMany;
import com.custom.orm.annotations.relations.OneToOne;
import com.custom.orm.enums.CascadeType;
import com.custom.orm.enums.FieldType;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

public class MetaDataManagerImpl implements MetaDataManager {

    /*
    * This method returns the name of the table in the database, which is the analog of the class on the Java application side.
    * If a class is annotated with the @Table annotation, the table name and database schema are taken from this annotation.
    * If the class is not annotated with the @Table annotation, the name of the table is taken from the name of that class.
    * */
    @Override
    public <T> String getTableName(Class<T> object) {
        return ofNullable(object.getAnnotation(Table.class))
                .map(tableAnnotation -> tableAnnotation.schema().equals("") ?
                        tableAnnotation.name() : tableAnnotation.schema() + "." + tableAnnotation.name())
                .orElse(object.getSimpleName().toLowerCase());
    }

    /*
    * This method returns the field name that is the primary key in the database.
    * This field is marked with the @Id annotation.
    * */
    @Override
    public <T> String getIdColumnName(Class<T> object) {
        return Arrays.stream(object.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("Entity doesn't have ID!"))
                .getName();
    }

    /*
    * This method returns the value of the field that is the primary key in the database.
    * This field is marked with the @Id annotation.
    * */
    @SneakyThrows
    @Override
    public <T> String getIdColumnValues(T object) {
        Field idField = Arrays.stream(object.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("Entity doesn't have ID!"));

        idField.setAccessible(true);

        return idField.get(object).toString();
    }

    /*
    * This method returns a string of "?" characters that correspond to the number of object fields that will need
    * to be passed in the SQL request to the PreparedStatement.
    * Fields of the object that are marked with the @Id annotation and the @OneToOne annotation,
    * but at the same time are not marked with the @JoinColumn annotation - these fields are skipped.
    * Object fields marked with the @JoinColumn annotation are taken into account.
    * */
    @SneakyThrows
    @Override
    public <T> String getColumnValues(T object) {
        List<Field> declaredFields = Arrays.asList(object.getClass().getDeclaredFields());

        return declaredFields.stream()
                .filter(field -> !field.isAnnotationPresent(Id.class))
                .filter(field -> !(field.isAnnotationPresent(OneToOne.class) && !field.isAnnotationPresent(JoinColumn.class)))
                .map(field -> "?")
                .collect(joining(", "));
    }

    /*
    * This method returns a string with the names of all the object's fields.
    * Fields of the object that are marked with the @Id annotation and the @OneToOne annotation,
    * but at the same time are not marked with the @JoinColumn annotation - these fields are skipped.
    * Object fields marked with the @JoinColumn annotation are taken into account.
    * */
    @Override
    public <T> String getColumnNames(T object) {
        Field[] declaredFields = object.getClass().getDeclaredFields();

        List<Field> collect = Arrays.stream(declaredFields)
                .filter(field -> !field.isAnnotationPresent(Id.class))
                .filter(field -> !(field.isAnnotationPresent(OneToOne.class) && !field.isAnnotationPresent(JoinColumn.class)))
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < collect.size(); i++) {
            if (collect.get(i).isAnnotationPresent(Column.class)) {
                sb.append(collect.get(i).getAnnotation(Column.class).name());
            } else if (collect.get(i).isAnnotationPresent(JoinColumn.class)) {
                sb.append(collect.get(i).getAnnotation(JoinColumn.class).name());
            } else {
                sb.append( collect.get(i).getName());
            }
            if (i < collect.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * This method returns the name of the given column.
     * It returns either name specified in @Column or the field's name itself.
     */
    @Override
    public String getColumnName(Field field) {
        return ofNullable(field.getAnnotation(JoinColumn.class))
                .map(JoinColumn::name)
                .orElse(ofNullable(field.getAnnotation(Column.class))
                        .filter(annotation -> annotation.name().length() > 0)
                        .map(Column::name)
                        .orElse(field.getName()));

    }

    /**
     * This method returns the name of the table corresponding to the entity of the given Class.
     * It returns either name specified in @Table or the entity's name itself.
     */
    @Override
    public <T> String getTableNameWithoutSchema(Class<T> object) {
        return ofNullable(object.getAnnotation(Table.class))
                .map(Table::name)
                .filter(name -> name.length() > 0)
                .orElse(object.getSimpleName().toLowerCase());
    }

    /*
    * Get all the declared fields of the object, excluding the fields when they are marked by the @OneToOne annotation
    * but not marked by the @JoinColumn annotation.
    * */
    @Override
    public <T> List<Field> getDeclaredFields(T object) {
        return Arrays.stream(object.getClass().getDeclaredFields())
                .filter(field -> !(field.isAnnotationPresent(OneToOne.class) && !field.isAnnotationPresent(JoinColumn.class)))
                .collect(Collectors.toList());
    }

    /*
    * Get declared fields that are marked with the @OneToOne annotation, but are not marked with the @JoinColumn annotation.
    * */
    @Override
    public <T> List<Field> getOneToOneDeclaredFields(T object) {
        return Arrays.stream(object.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToOne.class))
                .filter(field -> !field.isAnnotationPresent(JoinColumn.class))
                .collect(Collectors.toList());
    }

    /**
     * This method maps Java-types into SQL-types. It works with the limited (most vital) Java-types.
     * You may specify necessary SQL-type in @Column. Otherwise, it maps Java-types by switch-operator.
     * Please, note that if neither of switch-cases succeed, it presumes that field-type is another entity,
     * therefore it will be mapped into "BIGINT", so in database it will be used as id of that other entity
     * (if necessary - as foreign key). So be careful while using type that is not specified in switch-operator.
     * Please, note that field annotated with @Id are always mapped into "SERIAL", so it will be autoincrementing.
     */
    @Override
    public String getColumnType(Field field) {
        if (field.isAnnotationPresent(Column.class)
                && !(field.getAnnotation(Column.class).type().equals(FieldType.DEFAULT)))
            return field.getAnnotation(Column.class).type().toString();

        if (field.isAnnotationPresent(Id.class))
            return "SERIAL";

        String fieldType = field.getType().getSimpleName();

        switch (fieldType) {
            case ("Long"):
                return "BIGINT";
            case ("Integer"):
                return "INTEGER";
            case ("String"):
                return "VARCHAR";
            case ("LocalDateTime"):
                return "DATETIME";
            case ("LocalDate"):
                return "DATE";
            case ("LocalTime"):
                return "TIME";
            case ("Boolean"):
                return "BOOLEAN";
        }
        return "BIGINT";
    }

    /**
     * This method returns name of the column that is primary key.
     * It works only with primary key that consists of one column.
     */
    @Override
    public String getPrimaryKeyColumnName(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .map(this::getColumnName)
                .collect(joining());
    }

    /**
     * This method returns names of the columns that compose primary key.
     * It works only with primary key that consists of a few columns.
     * Please, note that in order to specify composed primary key, you need to use @ComposedPrimaryKey on each field.
     * Please, use @ComposedPrimaryKey only if your primary key consists of a few column. Never use this annotation for
     * a primary key that consists of only one column.
     */
    @Override
    public String getComposedPrimaryKeyColumnsNames(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ComposedPrimaryKey.class))
                .map(field -> ofNullable(field.getAnnotation(Column.class))
                        .map(Column::name)
                        .filter(name -> name.length() > 0)
                        .orElse(field.getName()))
                .collect(joining(", "));
    }

    /**
     * This method checks if given entity contains foreign key. It returns true if there is at least one @JoinColumn
     */
    @Override
    public boolean hasForeignKey(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .anyMatch(field -> field.isAnnotationPresent(JoinColumn.class));
    }

    /**
     * This method checks if given field is foreign key. It returns true if it is annotaded with @JoinColumn.
     */
    @Override
    public boolean isForeignKey(Field field) {
        return field.isAnnotationPresent(JoinColumn.class);
    }

    /**
     * This method returns set of the names of the columns that are foreign keys (in other words,
     * columns that contain foreign keys).
     */
    @Deprecated
    @Override
    public Set<String> getForeignKeyColumnNames(Class<?> entityClass) {
        if (!hasForeignKey(entityClass))
            throw new RuntimeException(
                    "Table " + getTableName(entityClass) + " does not contain foreign key!");

        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .map(field -> ofNullable(field.getAnnotation(Column.class))
                        .map(Column::name)
                        .filter(name -> name.length() > 0)
                        .orElse(field.getName()))
                .collect(Collectors.toSet());
    }

    /**
     * This method returns set of all fields of the given entity that are foreign key
     * (in other words, all fields that are annotated with @JoinColumn).
     */
    public Set<Field> getForeignKeyColumns(Class<?> entityClass) {
        if (!hasForeignKey(entityClass))
            throw new RuntimeException(
                    "Table " + getTableName(entityClass) + " does not contain foreign key!");

        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .collect(Collectors.toSet());
    }

    /**
     * This method returns set of the names of the constraints that specify foreign keys of the given entity.
     */
    @Override
    public Set<String> getForeignKeyNames(Class<?> entityClass) {
        if (!hasForeignKey(entityClass))
            throw new RuntimeException(
                    "Table " + getTableName(entityClass) + " does not contain foreign key!");

        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .map(field -> getForeignKeyName(entityClass, field))
                .collect(Collectors.toSet());
    }

    /**
     * This method returns name of the constraint that specifies foreign key of the given field.
     */
    @Override
    public String getForeignKeyName(Class<?> entityClass, Field field) {
        if (!isForeignKey(field))
            throw new RuntimeException(
                    "Column " + getColumnName(field) + " does not contain foreign key!");

        String result = "fk_%s_%s";
        return String.format(result, getForeignKeyReferenceClassName(field),
                getTableNameWithoutSchema(entityClass));
    }

    /**
     * This method returns set of the names of the classes that foreign keys of the given entity reference.
     */
    @Override
    public Set<String> getForeignKeyReferenceClassNames(Class<?> entityClass) {
        if (!hasForeignKey(entityClass))
            throw new RuntimeException(
                    "Table " + getTableName(entityClass) + " does not contain foreign key!");

        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .map(field -> getTableNameWithoutSchema(field.getType()))
                .collect(Collectors.toSet());
    }

    /**
     * This method returns name of the class that foreign key of the given field references.
     */
    @Override
    public String getForeignKeyReferenceClassName(Field field) {
        if (!isForeignKey(field))
            throw new RuntimeException(
                    "Column " + getColumnName(field) + " does not contain foreign key!");

        return getTableNameWithoutSchema(field.getType());
    }

    /**
     * This method returns set of the classes that foreign keys of the given entity reference.
     */
    @Override
    public Set<Class<?>> getForeignKeyReferenceClasses(Class<?> entityClass) {
        if (!hasForeignKey(entityClass))
            throw new RuntimeException(
                    "Table " + getTableName(entityClass) + " does not contain foreign key!");

        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .map(Field::getType)
                .collect(Collectors.toSet());
    }

    /**
     * This method returns the class that foreign key of the given field references.
     */
    @Override
    public <T> Class getForeignKeyReferenceClass(Field field) {
        if (!isForeignKey(field))
            throw new RuntimeException(
                    "Column " + getColumnName(field) + " does not contain foreign key!");

        return field.getType();
    }

    /**
     * This method returns set of names of the columns that foreign keys of the given entity reference
     * (usually they reference to the primary key).
     */
    @Override
    public Set<String> getForeignKeyReferenceColumnNames(Class<?> entityClass) {
        if (!hasForeignKey(entityClass))
            throw new RuntimeException(
                    "Table" + getTableName(entityClass) + "does not contain foreign key!");

        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .map(field -> getIdColumnName(field.getType()))
                .collect(Collectors.toSet());
    }

    /**
     * This method returns name of the column that foreign key of the given field references (usually it references
     * to the primary key).
     */
    @Override
    public String getForeignKeyReferenceColumnName(Field field) {
        if (!isForeignKey(field))
            throw new RuntimeException(
                    "Column " + getColumnName(field) + " does not contain foreign key!");

        return getIdColumnName(field.getType());
    }


    /**
     * This method returns a set of class names of fields of the given entity that are annotated with @OneToOne.
     */
    @Override
    public <T> Set<String> getOneToOneForeignKeyClassNames(Class<T> entityClass) {
        if (Arrays.stream(entityClass.getDeclaredFields())
                .anyMatch(field -> field.isAnnotationPresent(OneToOne.class)))

            return Arrays.stream(entityClass.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(OneToOne.class))
                    .filter(field -> Arrays.asList(field.getAnnotation(OneToOne.class).cascade()).contains(CascadeType.ALL) ||
                            Arrays.asList(field.getAnnotation(OneToOne.class).cascade()).contains(CascadeType.GET))
                    .filter(field -> field.getAnnotation(OneToOne.class).mappedBy().length() > 0)
                    .map(field -> field.getType().getName())
                    .collect(Collectors.toSet());

        return new HashSet<>();
    }

    /**
     * This method returns a set of class names of fields of the given entity that are annotated with @OneToMany.
     */
    @Override
    public <T> Set<String> getOneToManyForeignKeyClassNames(Class<T> entityClass) {
        if (Arrays.stream(entityClass.getDeclaredFields())
                .anyMatch(field -> field.isAnnotationPresent(OneToMany.class) &&
                        (Arrays.asList(field.getAnnotation(OneToMany.class).cascade()).contains(CascadeType.ALL) ||
                                Arrays.asList(field.getAnnotation(OneToMany.class).cascade()).contains(CascadeType.GET))
                )
        )
            return Arrays.stream(entityClass.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(OneToMany.class))
                    .filter(field -> field.getAnnotation(OneToMany.class).mappedBy().length() > 0)
                    .map(field -> field.getType().getName())
                    .collect(Collectors.toSet());

        return new HashSet<>();
    }

    /**
     * This method returns a set of class names of fields of the given entity that are annotated with @ManyToOne.
     */
    @Override
    public <T> Set<String> getManyToOneForeignKeyClassNames(Class<T> entityClass) {
        if (Arrays.stream(entityClass.getDeclaredFields())
                .anyMatch(field -> field.isAnnotationPresent(ManyToOne.class)))
            return Arrays.stream(entityClass.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(ManyToOne.class))
                    .filter(field -> field.getAnnotation(ManyToOne.class).mappedBy().length() > 0)
                    .map(field -> field.getType().getName())
                    .collect(Collectors.toSet());

        return new HashSet<>();
    }

    public <T> boolean tableExists (Connection connection, Class<T> entityClass) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet resultSet = metaData.getTables(
                null, null, getTableNameWithoutSchema(entityClass), null);

        return resultSet.next();
    }
}