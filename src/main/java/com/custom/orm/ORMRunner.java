package com.custom.orm;

import com.custom.orm.entity.User;
import com.custom.orm.sessions.Session;
import com.custom.orm.sessions.SessionImpl;

import java.time.LocalDate;

public class ORMRunner {

    public static void main(String[] args) {

        User user = User.builder()
                .id(3L)
                .firstname("Ivan")
                .lastname("Ivanov")
                .birthDate(LocalDate.of(2000, 1, 10))
                .age(22)
                .build();


        Session session = new SessionImpl();
        session.create(user);
        //session.delete(user);
    }
}
