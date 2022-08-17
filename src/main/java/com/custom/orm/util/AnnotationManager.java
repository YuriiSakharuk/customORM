package com.custom.orm.util;

import com.custom.orm.annotations.Column;
import com.custom.orm.annotations.Table;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class AnnotationManager {
    //todo: method that create table:
    //1) scan package for annotation "@Table"
    //2) scan class marked with annotation "@Table" for annotations "@Column"
    //3) create SQL-query that create corresponding table in DB

    public static <T> StringBuilder createTable(T entity){
        Class<?> entityClass = entity.getClass();
        Table table = entityClass.getAnnotation(Table.class);
        StringBuilder query = new StringBuilder();
        if (table == null)
            throw new RuntimeException("Entity \"" + entityClass.getName() + "\" not found!");
        else {
            String tableName = table.name();
            if (tableName.length() == 0)
                tableName = entityClass.getSimpleName();
            String columnDefinition = "";
            query.append("CREATE TABLE IF NOT EXISTS " + tableName + " (");

            for (Field field : entityClass.getDeclaredFields()) {
                String columnName;
                Annotation[] annotations = field.getDeclaredAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation instanceof Column) {
                        if (((Column) annotation).name().length() == 0)
                            columnName = field.getName();
                        else columnName = ((Column) annotation).name();

                        columnDefinition = columnName + " " + ((Column) annotation).type() + getConstraints(annotation);
                    }
                }
                query.append(columnDefinition).append(", ");
            }
            query.append(getPrimaryKey(entityClass));
            query.append(");");
        }
        return query;
    }

    private static StringBuilder getPrimaryKey(Class<?> entityClass) {
        StringBuilder primaryKeyQuery = new StringBuilder("CONSTRAINT pk_" + entityClass.getSimpleName()
                + " PRIMARY KEY (");
        for (Field field : entityClass.getDeclaredFields()) {
            Column columnAnnotation = field.getAnnotation(Column.class);

            if (columnAnnotation.primaryKey())
                primaryKeyQuery.append((columnAnnotation.name().length() == 0
                        ? field.getName() : columnAnnotation.name()) + ", ");
        }
        primaryKeyQuery.delete(primaryKeyQuery.length() - 2, primaryKeyQuery.length());
        primaryKeyQuery.append(")");

        return primaryKeyQuery;
    }

    private static StringBuilder getConstraints(Annotation annotation) {
        StringBuilder constraintQuery = new StringBuilder();
        Column column = (Column) annotation;

        if (!column.nullable())
            constraintQuery.append(" NOT NULL");

        if(column.unique())
            constraintQuery.append(" UNIQUE");

        if (column.id())
            constraintQuery.append(" GENERATED ALWAYS AS IDENTITY");

        return constraintQuery;
    }


    public static void main(String[] args){
        SomeEntity someEntity = new SomeEntity();
        System.out.println(createTable(someEntity));

    }

}
