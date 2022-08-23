package com.custom.orm;

import com.custom.orm.entity.Profile;
import com.custom.orm.entity.User;
import com.custom.orm.sessions.Session;
import com.custom.orm.sessions.SessionImpl;
import com.custom.orm.sessions.Transaction;

import java.time.LocalDate;

public class ORMRunnerOneToOne {

    public static void main(String[] args) {

        //Create objects
        User user = User.builder()
                .id(1L)
                .firstname("Stepan")
                .lastname("Giga")
                .birthDate(LocalDate.of(2000, 1, 20))
                .age(600)
                .build();

        Profile profile = Profile.builder()
                .passport("BC254125")
                .build();

        profile.setUser(user);

        Session session = new SessionImpl();
        Transaction transaction = session.beginTransaction();

        //Inserting an object into a table
        session.create(user);

        transaction.commit();

        session.close();
    }
}
