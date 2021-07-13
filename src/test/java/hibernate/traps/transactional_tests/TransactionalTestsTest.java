package hibernate.traps.transactional_tests;

import hibernate.traps.JpaTransactionWrapper;
import hibernate.traps.transactional_tests.config.TransactionalTestsConfig;
import hibernate.traps.transactional_tests.dto.UserDto;
import hibernate.traps.transactional_tests.model.Address;
import hibernate.traps.transactional_tests.model.User;
import hibernate.traps.transactional_tests.service.UserService;
import hibernate.traps.transactional_tests.util.TestDatabaseUtil;
import org.assertj.core.util.Lists;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TransactionalTestsConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TransactionalTestsTest {

    private static final String USER_NAME = "userName";

    @Autowired
    private UserService userService;

    @Autowired
    private DataSource dataSource;

    @Test
    @Transactional
    /*
     * This test IS Transactional AND calls methods that use Transactions propagated from the caller.
     * This kind of testing seems to be the most convenient, because each test is automatically rolled back by Spring
     * and we don't need to worry about polluting test database with data from other tests. This however comes with
     * a very serious cost of not being able to test the actual Production-like Transaction propagation.
     */
    public void integrationTestWithNotActualProductionTransactionScopeAndPropagatedTransaction() {
        //GIVEN: There is a User with 2 addresses
        userService.createUserInPropagatedTransaction(getNewUser());

        //WHEN: user is fetched by name
        Optional<User> u = userService.getUserByName(USER_NAME);

        //THEN: user is correctly fetched
        assertTrue(u.isPresent());
        assertEquals(USER_NAME, u.get().getName());

        //AND: unlike in production code, no exception is thrown when trying to access lazy-initialized properties
        userService.printUser(u.get());
    }

    @Test
    @Transactional
    /*
     * This test IS Transactional AND calls methods that explicitly use NEW Transactions, not propagated from the caller.
     * Changing the Propagation of the underlying Transaction didn't fix the problem with @Transactional integration test.
     * The results are still different from the actual Production-like scenario.
     * Additionally we need to manually clean up the database.
     */
    public void integrationTestWithNotActualProductionTransactionScopeButExplicitlyNewTransaction() {
        //GIVEN: There is a User with 2 addresses created within a separate transaction
        userService.createUserInExplicitlyNewTransaction(getNewUser());

        //WHEN: user is fetched by name
        Optional<User> u = userService.getUserByName(USER_NAME);

        //THEN: user is correctly fetched with its addresses
        assertTrue(u.isPresent());
        assertEquals(USER_NAME, u.get().getName());

        //AND: unlike in production code, no exception is thrown when trying to access lazy-initialized properties
        userService.printUser(u.get());

        //CLEANUP: we need to manually clean the database, because although automatic rollback is performed, the explicitly new Transaction was already committed
        //and won't be rolled back by Spring
        TestDatabaseUtil.resetDatabase(dataSource);
    }


    @Test
    /*
     * This test IS NOT Transactional and calls methods that use Transactions propagated from the caller.
     * This kind of testing requires a little more work on our side as we need to manually clean up the database after
     * the test is finished. It rewards us though with the ability to test the actual Production-like Transaction propagation
     */
    public void integrationTestWithActualProductionTransactionScopeAndPropagatedTransaction() {
        //GIVEN: There is a User with 2 addresses created within a separate transaction
        userService.createUserInPropagatedTransaction(getNewUser());

        //WHEN: user is fetched by name
        Optional<User> u = userService.getUserByName(USER_NAME);

        //THEN: user is correctly fetched with its addresses
        assertTrue(u.isPresent());
        assertEquals(USER_NAME, u.get().getName());

        //AND: like in production code, an exception is thrown when trying to access lazy-initialized properties
        assertThrows(LazyInitializationException.class, () -> userService.printUser(u.get()));

        //CLEANUP: we need to manually clean the database, because no autmatic rollback is performed
        TestDatabaseUtil.resetDatabase(dataSource);
    }


    private UserDto getNewUser() {
        return new UserDto(USER_NAME, Lists.newArrayList("a1", "a2"));
    }
}
