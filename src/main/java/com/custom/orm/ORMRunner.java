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
        User user1 = User.builder()
                .id(1L)
                .firstname("Stepan")
                .lastname("Giga")
                .birthDate(LocalDate.of(2000, 1, 20))
                .age(600)
                .build();

        User user2 = User.builder()
                .id(1L)
                .age(350)
                .build();

        Session session = new SessionImpl();

        Transaction transaction = session.beginTransaction();

        //Inserting an object into a table
        session.create(user1);
        transaction.commit();

        //Find an entry in the table by id
        transaction = session.beginTransaction();
        User userFromDB = session.findById(User.class , 1L);
        System.out.println(userFromDB.toString());
        transaction.commit();

        //Update table
        transaction = session.beginTransaction();
        session.update(user2);
        transaction.commit();

        //Find all entries in the table
        transaction = session.beginTransaction();
        List<User> usersList = session.findAll(User.class);
        System.out.println(usersList.toString());
        transaction.commit();

        //Delete an entry in the table
        transaction = session.beginTransaction();
        session.delete(user1);
        transaction.commit();

        session.close();
    }
}
