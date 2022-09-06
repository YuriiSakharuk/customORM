package com.custom.orm;

import com.custom.orm.entity.User;
import com.custom.orm.sessions.Session;
import com.custom.orm.sessions.SessionImpl;
import com.custom.orm.sessions.Transaction;

import java.time.LocalDate;
import java.util.List;

public class ORMRunner {

    public static void main(String[] args) {

        //Create objects
        User user = User.builder()
                .firstname("Stepan")
                .lastname("Giga")
                .birthDate(LocalDate.of(2000, 1, 20))
                .age(400)
                .build();

        Session session = new SessionImpl();

        //Inserting an object into a table
        System.out.println("-----Inserting an object into a table-----");
        Transaction transaction = session.beginTransaction();
        session.create(user);
        transaction.commit();

        //Find an entry in the table by id
        System.out.println("-----Find an entry in the table by id-----");
        transaction = session.beginTransaction();
        User userFromDB = session.findById(User.class , user.getId());
        System.out.println("User from DB: " + userFromDB.toString());
        transaction.commit();

        //Update table
        user.setAge(200);
        System.out.println("-----Update table-----");
        transaction = session.beginTransaction();
        session.update(user);
        System.out.println("Update user age: " + user.getAge());
        transaction.commit();

        //Find all entries in the table
        System.out.println("-----Find all entries in the table-----");
        transaction = session.beginTransaction();
        List<User> usersList = session.findAll(User.class);
        System.out.println("Collection of entities obtained from the database: " + usersList);
        transaction.commit();

        //Delete an entry in the table
        System.out.println("----Delete an entry in the table-----");
        transaction = session.beginTransaction();
        session.delete(user);
        transaction.commit();

        session.close();
    }
}
