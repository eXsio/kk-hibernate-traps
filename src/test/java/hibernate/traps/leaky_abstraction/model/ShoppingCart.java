
package hibernate.traps.leaky_abstraction.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SHOPPING_CARTS")
public class ShoppingCart implements Serializable {

    @Id
    @Column(name = "CART_ID")
    @GeneratedValue
    private Long id;

    @OneToMany(mappedBy = "shoppingCart", cascade = CascadeType.ALL)
    private List<CartItem> items = new ArrayList<>();


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public void addItem(CartItem item) {
        items.add(item);
        item.setShoppingCart(this);
    }
}
