package com.custom.orm;

import com.custom.orm.entity.Profile;
import com.custom.orm.entity.User;
import com.custom.orm.sessions.Session;
import com.custom.orm.sessions.SessionImpl;
import com.custom.orm.sessions.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

public class ORMRunnerOneToOne {

    public static void main(String[] args) {
        final Logger log = LoggerFactory.getLogger(ORMRunnerOneToOne.class);

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

        Session session = new SessionImpl();
        Transaction transaction = session.beginTransaction();

        //Inserting an object into a table
        log.info("-----Inserting an object into a table-----");
        session.create(user);
        transaction.commit();

        //Find an entry in the table by id
        log.info("-----Find an entry in the table by id-----");
        User userFromDB = session.findById(User.class , user.getId());
        log.info("User from DB: " + userFromDB.toString());
        transaction.commit();

        //Find all entries in the table
        log.info("----Find all entries in the table----");
        transaction = session.beginTransaction();
        List<User> usersList = session.findAll(User.class);
        log.info("Collection of entities obtained from the database: " + usersList);
        transaction.commit();

        //Delete an entry in the table
        log.info("----Delete an entry in the table-----");
        transaction = session.beginTransaction();
        session.delete(user);
        transaction.commit();

        session.close();
    }
}
