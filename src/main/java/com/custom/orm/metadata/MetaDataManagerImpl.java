package com.custom.orm.metadata;

import com.custom.orm.annotations.Column;
import com.custom.orm.annotations.ComposedPrimaryKey;
import com.custom.orm.annotations.Id;
import com.custom.orm.annotations.Table;
import com.custom.orm.annotations.relations.JoinColumn;
import com.custom.orm.annotations.relations.ManyToOne;
import com.custom.orm.annotations.relations.OneToMany;
import com.custom.orm.annotations.relations.OneToOne;
import com.custom.orm.enums.FieldType;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

public class MetaDataManagerImpl implements MetaDataManager {

    @Override
    public <T> String getTableName(Class<T> object) {
        return ofNullable(object.getAnnotation(Table.class))
                .map(tableAnnotation -> tableAnnotation.schema().equals("") ?
                        tableAnnotation.name() : tableAnnotation.schema() + "." + tableAnnotation.name())
                .orElse(object.getSimpleName().toLowerCase());
    }

    @Override
    public <T> String getIdColumnName(Class<T> object) {
        return Arrays.stream(object.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("Entity doesn't have ID!"))
                .getName();
    }

    @Override
    public <T> String getIdColumnValues(T object) throws IllegalAccessException {
        Field idField = Arrays.stream(object.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("Entity doesn't have ID!"));

        idField.setAccessible(true);

        return idField.get(object).toString();
    }

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

    @Override
    public <T> String getColumnNames(T object) {
        Field[] declaredFields = object.getClass().getDeclaredFields();

        List<Field> collect = Arrays.stream(declaredFields).filter(field -> !field.isAnnotationPresent(Id.class))
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
        return ofNullable(field.getAnnotation(Column.class))
                .map(Column::name)
                .filter(name -> name.length() > 0)
                .orElse(field.getName());
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
     * This method returns name of the column that is foreign key (in other words, column that contains foreign key).
     * Please, note that it works only, if entity has one @JoinColumn. Otherwise, all names will be joined together,
     * and it will cause incorrect work of the application.
     * THIS METHOD WILL BE REWRITTEN!
     */
    @Override
    @Deprecated
    public String getForeignKeyColumnName(Class<?> entityClass) {
        if (!hasForeignKey(entityClass))
            throw new RuntimeException(
                    "Table " + getTableName(entityClass) + " does not contain foreign key!");

        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .map(field -> ofNullable(field.getAnnotation(Column.class))
                        .map(Column::name)
                        .filter(name -> name.length() > 0)
                        .orElse(field.getName()))
                .collect(joining());
    }

    /**
     * This method returns name of the constraint that specifies foreign key.
     * Please, note that it works only, if entity has one @JoinColumn. Otherwise, all names will be joined together,
     * and it will cause incorrect work of the application.
     * THIS METHOD WILL BE REWRITTEN!
     */
    @Override
    @Deprecated
    public String getForeignKeyName(Class<?> entityClass) {
        if (!hasForeignKey(entityClass))
            throw new RuntimeException(
                    "Table " + getTableName(entityClass) + " does not contain foreign key!");

        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .map(field -> field.getAnnotation(JoinColumn.class).name())
                .collect(joining());
    }

    /**
     * This method returns name of the class that foreign key of the given entity references.
     * Please, note that it works only, if entity has one @JoinColumn. Otherwise, it will pick only one first name,
     * and it will cause incorrect work of the application.
     * THIS METHOD WILL BE REWRITTEN!
     */
    @Override
    @Deprecated
    public String getForeignKeyReferenceClassName(Class<?> entityClass) {
        if (!hasForeignKey(entityClass))
            throw new RuntimeException(
                    "Table " + getTableName(entityClass) + " does not contain foreign key!");

        Class<?> foreignKeyClass = Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .findAny().get().getType();

        return getTableNameWithoutSchema(foreignKeyClass);
    }

    /**
     * This method returns the class that foreign key of the given entity references.
     * Please, note that it works only, if entity has one @JoinColumn. Otherwise, it will pick only one first class,
     * and it will cause incorrect work of the application.
     * THIS METHOD WILL BE REWRITTEN!
     */
    @Override
    @Deprecated
    public <T> Class getForeignKeyReferenceClass(Class<T> entityClass) {
        if (!hasForeignKey(entityClass))
            throw new RuntimeException(
                    "Table " + getTableName(entityClass) + " does not contain foreign key!");

        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .findAny().get().getType();
    }

    /**
     * This method returns name of the column that foreign key of the given entity references.
     * Please, note that it works only, if entity has one @JoinColumn. Otherwise, it will pick only one first name,
     * and it will cause incorrect work of the application.
     * THIS METHOD WILL BE REWRITTEN!
     */
    @Override
    @Deprecated
    public String getForeignKeyReferenceColumnName(Class<?> entityClass) {
        if (!hasForeignKey(entityClass))
            throw new RuntimeException(
                    "Table" + getTableName(entityClass) + "does not contain foreign key!");

        Class<?> foreignKeyClass = Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .findAny().get().getType();

        return getIdColumnName(foreignKeyClass);
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
                .anyMatch(field -> field.isAnnotationPresent(OneToMany.class)))
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
}