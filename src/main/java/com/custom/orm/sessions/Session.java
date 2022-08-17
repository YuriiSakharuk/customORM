package com.custom.orm.sessions;


import java.util.List;

public interface Session{

    <T> T findById(Class<T> objectClass, Object key);

    <T> List<T> findAll(Class<T> objectClass);

    <T> void create(T object);

    <T> boolean update(T object);

    <T> boolean delete(T object);
}
