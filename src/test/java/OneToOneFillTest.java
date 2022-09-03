import com.custom.orm.entity.Profile;
import com.custom.orm.entity.User;
import com.custom.orm.sessions.Session;
import com.custom.orm.sessions.SessionImpl;
import com.custom.orm.sessions.Transaction;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OneToOneFillTest {

    User user;
    Profile profile;

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
    public void test() {
        Session session = new SessionImpl();
        Transaction transaction = session.beginTransaction();

        session.create(user);
        transaction.commit();

        transaction = session.beginTransaction();
        User user1 = session.findById(User.class, user.getId());
        transaction.commit();
        assertEquals(user.getId(), user1.getId());
        assertEquals(user.getFirstname(), user1.getFirstname());
        assertEquals(user.getLastname(), user1.getLastname());
        assertEquals(user.getBirthDate(), user1.getBirthDate());
        assertEquals(user.getAge(), user1.getAge());
        assertEquals(user.getProfile().getId(), user1.getProfile().getId());
        assertEquals(user.getProfile().getPassport(), user1.getProfile().getPassport());

        transaction = session.beginTransaction();
        session.delete(user);
        transaction.commit();

        transaction = session.beginTransaction();
        User user2 = session.findById(User.class, user.getId());
        transaction.commit();
        assertNull(user2);

        transaction = session.beginTransaction();
        for (int i = 0; i < 3; i++) {
            session.create(user);
            session.create(user);
            session.create(user);
        }
        transaction.commit();

        transaction = session.beginTransaction();
        List<User> users = session.findAll(User.class);
        transaction.commit();
        session.close();

        User lastUser = users.stream()
                .sorted(Comparator.comparing(User::getId))
                .collect(Collectors.toList())
                .get(users.size()-1);
        assertEquals(user.getId(), lastUser.getId());
        assertEquals(user.getFirstname(), lastUser.getFirstname());
        assertEquals(user.getLastname(), lastUser.getLastname());
        assertEquals(user.getBirthDate(), lastUser.getBirthDate());
        assertEquals(user.getAge(), lastUser.getAge());
        assertEquals(user.getProfile().getId(), lastUser.getProfile().getId());
        assertEquals(user.getProfile().getPassport(), lastUser.getProfile().getPassport());
    }
}
