package com.custom.orm.util;

import com.custom.orm.annotations.Column;
import com.custom.orm.annotations.Table;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            query.append("CREATE TABLE IF NOT EXISTS " + tableName + "(");

            for (Field field : entityClass.getDeclaredFields()) {
                String columnName = "";
                Annotation[] annotations = field.getDeclaredAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation instanceof Column) {
                        if (((Column) annotation).name().length() == 0)
                            columnName = field.getName();
                        else columnName = ((Column) annotation).name();

                        columnDefinition = columnName + " " + ((Column) annotation).type();
                    }
                }
                query.append(columnDefinition).append(", ");
            }
            query.delete(query.length() - 2, query.length())
                    .append(");");
        }
        return query;
    }


    public static void main(String[] args){
        SomeEntity someEntity = new SomeEntity();
        System.out.println(createTable(someEntity));

    }

}
