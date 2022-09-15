package com.custom.orm.sessions;


import java.util.List;

public interface Session {

    <T> T findById(Class<T> objectClass, Long key);

    <T> List<T> findAll(Class<T> objectClass);

    <T> boolean create(T object);

    <T> void update(T object);

    <T> void delete(T object);

    Transaction beginTransaction();

    void close();

    void cancelQuery();
}
