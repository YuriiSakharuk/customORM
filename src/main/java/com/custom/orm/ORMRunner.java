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
                .age(600)
                .build();

        Session session = new SessionImpl();

        Transaction transaction = session.beginTransaction();

        //Inserting an object into a table
        session.create(user);
        transaction.commit();

        //Find an entry in the table by id
        transaction = session.beginTransaction();
        User userFromDB = session.findById(User.class , user.getId());
        System.out.println(userFromDB.toString());
        transaction.commit();

        user.setAge(300);

        //Update table
        transaction = session.beginTransaction();
        session.update(user);
        transaction.commit();

        //Find all entries in the table
        transaction = session.beginTransaction();
        List<User> usersList = session.findAll(User.class);
        System.out.println(usersList.toString());
        transaction.commit();

        //Delete an entry in the table
        transaction = session.beginTransaction();
        session.delete(user);
        transaction.commit();

        session.close();
    }
}
