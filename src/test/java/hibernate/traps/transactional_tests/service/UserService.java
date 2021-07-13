package hibernate.traps.transactional_tests.service;

import com.google.gson.Gson;
import hibernate.traps.transactional_tests.dto.UserDto;
import hibernate.traps.transactional_tests.model.Address;
import hibernate.traps.transactional_tests.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@Service
public class UserService {

    @PersistenceContext
    private EntityManager entityManager;

    public void createUser(UserDto userDto) {
        User u = new User(userDto.getName());
        userDto.getAddresses().forEach(a -> u.addAddress(new Address(a)));
        entityManager.persist(u);
    }

    public Optional<User> getUserByName(String name) {
        return entityManager.createQuery("from User u where u.name = :name", User.class)
                .setParameter("name", name)
                .getResultList()
                .stream()
                .findFirst();
    }
}
