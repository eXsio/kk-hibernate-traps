package hibernate.traps.leaky_abstraction;

import hibernate.traps.JpaTransactionWrapper;
import hibernate.traps.SQLStatementLoggingAppender;
import hibernate.traps.leaky_abstraction.model.Product;
import hibernate.traps.leaky_abstraction.model.ShoppingCart;
import hibernate.traps.leaky_abstraction.service.ShoppingCartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LeakyAbstractionTest {

    private final static String PRODUCT_NAME = "P1";

    private final static Long QUANTITY = 3L;

    private EntityManagerFactory emf;

    private JpaTransactionWrapper jpaTransactionWrapper;

    private Long cartId;

    @BeforeEach
    public void init() {
        //Setup JPA with Hibernate
        emf = Persistence.createEntityManagerFactory("instruction_call_order_autoflush");
        jpaTransactionWrapper = new JpaTransactionWrapper(emf);

        //Add new Product to the Catalog
        jpaTransactionWrapper.doInTransaction(em -> {
            Product p1 = new Product(PRODUCT_NAME, BigDecimal.ONE);
            em.persist(p1);
        });

        //Create a new instance of a Shopping Cart
        ShoppingCart shoppingCart = jpaTransactionWrapper.doInTransactionAndReturn(em -> {
            ShoppingCart sc = new ShoppingCart();
            em.persist(sc);
            return sc;
        });

        cartId = shoppingCart.getId();
    }

    @Test
    public void shouldExecuteWithCorrectInstructionCallOrder() {
        //GIVEN: The Cart is empty
        assertEquals(0L, (long) countItems(cartId));

        //WHEN: a new Item is added to the Cart, using correct Java instruction call order
        jpaTransactionWrapper.doInTransaction(em ->
                new ShoppingCartService(em).addProductToCart_correctInstructionOrder(cartId, PRODUCT_NAME, QUANTITY)
        );

        //THEN: Item was successfully added to the Cart
        assertEquals(1L, (long) countItems(cartId));
    }

    @Test
    public void shouldThrowExceptionWithInvalidInstructionCallOrder() {
        //GIVEN: The Cart is empty
        assertEquals(0L, (long) countItems(cartId));

        //WHEN:  a new Item is added to the Cart, using invalid Java instruction call order
        assertThrows(PersistenceException.class, () ->
                jpaTransactionWrapper.doInTransaction(em ->
                        new ShoppingCartService(em).addProductToCart_invalidInstructionOrder(cartId, PRODUCT_NAME, QUANTITY)
                )
        );

        //THEN: An Exception was thrown due to Hibernate's inner logic that was not obvious from the Code point of view
        assertEquals(0L, (long) countItems(cartId));
    }

    private Long countItems(Long cartId) {
        EntityManager em = emf.createEntityManager();
        Long itemCount = new ShoppingCartService(em).countCartItems(cartId);
        em.close();
        return itemCount;
    }
}
