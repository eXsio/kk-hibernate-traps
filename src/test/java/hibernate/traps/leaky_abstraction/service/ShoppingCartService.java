package hibernate.traps.leaky_abstraction.service;


import hibernate.traps.leaky_abstraction.model.CartItem;
import hibernate.traps.leaky_abstraction.model.Product;
import hibernate.traps.leaky_abstraction.model.ShoppingCart;

import javax.persistence.EntityManager;

public class ShoppingCartService {

    private final EntityManager em;

    public ShoppingCartService(EntityManager em) {
        this.em = em;
    }

    /**
     * This is a valid and working scenario, where the Cart Item is being added after it has been correctly
     * initialized and populated with data.
     */
    public void addProductToCart_correctInstructionOrder(Long cartId, String productName, Long quantity) {
        ShoppingCart shoppingCart = getCart(cartId);
        CartItem ci = new CartItem();
        //Cart Item being set up before adding it to the Cart
        ci.setProduct(getProductByName(productName));
        ci.setQuantity(quantity);
        shoppingCart.addItem(ci);
    }

    /**
     * This scenario, although valid from Java point of view, will cause an Exception, because an un-populated Cart Item
     * is added to the Cart. During the Item population, the 'getProductByName' method is called. Hibernate's default behavior
     * will see, that there is a new Entity to be inserted prior to querying the Database. Depending on whether our Entity Mappings are
     * in sync with the actual Database Schema, we will get a PropertyValueException or SQLException. Both will be Runtime Exceptions.
     */
    public void addProductToCart_invalidInstructionOrder(Long cartId, String productName, Long quantity) {
        ShoppingCart shoppingCart = getCart(cartId);
        CartItem ci = new CartItem();
        shoppingCart.addItem(ci);
        //Cart Item being set up after adding it to the Cart
        ci.setProduct(getProductByName(productName));
        ci.setQuantity(quantity);
    }

    private ShoppingCart getCart(Long cartId) {
        return em.find(ShoppingCart.class, cartId);
    }

    private Product getProductByName(String productName) {
        return em.createQuery("select p from Product p where p.name = :name", Product.class)
                .setParameter("name", productName)
                .getSingleResult();

    }

    public Long countCartItems(Long cartId) {
        return em.createQuery("select count(ci.id) from CartItem ci where ci.shoppingCart.id = :cartId", Long.class)
                .setParameter("cartId", cartId)
                .getSingleResult();
    }
}
