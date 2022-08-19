package com.custom.orm;

import com.custom.orm.entity.User;
import com.custom.orm.sessions.Session;
import com.custom.orm.sessions.SessionImpl;

import java.time.LocalDate;
import java.util.List;

public class ORMRunner {

    public static void main(String[] args) {

        //Create objects
        User user1 = User.builder()
                .id(1L)
                .firstname("Stepan")
                .lastname("Banderas")
                .birthDate(LocalDate.of(2000, 1, 20))
                .age(600)
                .build();

        User user2 = User.builder()
                .id(1L)
                .age(350)
                .build();

        Session session = new SessionImpl();

        //Inserting an object into a table
        session.create(user1);

        //Find an entry in the table by id
        User userFromDB = session.findById(User.class , 1L);
        System.out.println(userFromDB.toString());

        //Update table
        session.update(user2);

        //Find all entries in the table
        List<User> usersList = session.findAll(User.class);
        System.out.println(usersList.toString());

        //Delete an entry in the table
        session.delete(user1);
    }
}
