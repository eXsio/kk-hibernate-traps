package hibernate.traps.transactional_tests.service;

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

    @Transactional
    public void createUserInPropagatedTransaction(UserDto userDto) {
        User u = new User(userDto.getName());
        userDto.getAddresses().forEach(a -> u.addAddress(new Address(a)));
        entityManager.persist(u);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createUserInExplicitlyNewTransaction(UserDto userDto) {
        User u = new User(userDto.getName());
        userDto.getAddresses().forEach(a -> u.addAddress(new Address(a)));
        entityManager.persist(u);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByName(String name) {
        return entityManager.createQuery("from User u where u.name = :name", User.class)
                .setParameter("name", name)
                .getResultList()
                .stream()
                .findFirst()
                .map(u -> {
                    // ensure no lazy loading
                    entityManager.detach(u);
                    return u;
                });
    }

    public void printUser(User u) {
        System.out.println(String.format("User %s with id %d has %d addresses.", u.getName(), u.getId(), u.getAddresses().size()));
    }
}
