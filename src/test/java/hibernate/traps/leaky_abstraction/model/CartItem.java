
package hibernate.traps.leaky_abstraction.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "CART_ITEMS")
public class CartItem implements Serializable {

    @Id
    @Column(name = "CART_ITEM_ID")
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID"//, nullable = false
    )
    private Product product;

    @ManyToOne
    @JoinColumn(name = "CART_ID"//, nullable = false
    )
    private ShoppingCart shoppingCart;

    @Column(name = "QTY"//, nullable = false
    )
    private Long quantity;

    public CartItem() {
    }

    public CartItem(Product product, ShoppingCart shoppingCart, Long quantity) {
        this.product = product;
        this.shoppingCart = shoppingCart;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public ShoppingCart getShoppingCart() {
        return shoppingCart;
    }

    public void setShoppingCart(ShoppingCart shoppingCart) {
        this.shoppingCart = shoppingCart;
    }
}
