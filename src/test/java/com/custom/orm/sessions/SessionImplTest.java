package com.custom.orm.sessions;

import com.custom.orm.entity.Profile;
import com.custom.orm.entity.User;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertTrue;

public class SessionImplTest {

    private User user;
    private Profile profile;

    @Before
    public void init() {
        user = User.builder()
                .firstname("Stepan")
                .lastname("Bandera")
                .birthDate(LocalDate.of(1921, 1, 20))
                .age(600)
                .build();

        profile = Profile.builder()
                .passport("BC254125")
                .build();

        profile.setUser(user);

    }

    @Test
    public void create(){
        Session session = new SessionImpl();
        Transaction transaction = session.beginTransaction();
        boolean check = session.create(user);
        transaction.commit();

        assertTrue(check);
    }
}