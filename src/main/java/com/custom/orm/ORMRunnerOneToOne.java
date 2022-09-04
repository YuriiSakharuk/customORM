package com.custom.orm;

import com.custom.orm.entity.Profile;
import com.custom.orm.entity.User;
import com.custom.orm.sessions.Session;
import com.custom.orm.sessions.SessionImpl;
import com.custom.orm.sessions.Transaction;

import java.time.LocalDate;
import java.util.List;

public class ORMRunnerOneToOne {

    public static void main(String[] args) {

        //Create objects
        User user = User.builder()
                .firstname("Stepan")
                .lastname("Bandera")
                .birthDate(LocalDate.of(1921, 1, 20))
                .age(600)
                .build();
        Profile profile = Profile.builder()
                .passport("BC254125")
                .build();
        profile.setUser(user);

        System.out.println("User: " + user.getFirstname() + " " + user.getLastname());
        System.out.println("User passport: " + user.getProfile().getPassport());

        Session session = new SessionImpl();
        Transaction transaction = session.beginTransaction();

        //Inserting an object into a table
        session.create(user);
        transaction.commit();

        // the same as user
        User user1 = session.findById(User.class, user.getId());
        System.out.println("User after creation: " + user1.getFirstname() + " " + user1.getLastname());
        System.out.println("User passport: " + user1.getProfile().getPassport());
        transaction.commit();

        //Delete an entry in the table
        transaction = session.beginTransaction();
        session.delete(user);
        transaction.commit();

        transaction = session.beginTransaction();
        User user2 = session.findById(User.class, user.getId());
        System.out.println("User after delete: " + user2); // null
        transaction.commit();
        session.close();
    }
}
