package hibernate.traps.transactional_tests.model;

import javax.persistence.*;

@Entity
@Table(name = "APP_ADDRESSES")
public class Address {

    @Id
    @Column(name = "ADDRESS_ID", nullable = false)
    @GeneratedValue
    private Long id;

    @Column(name = "ADDR", nullable = false)
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    public Address() {
    }

    public Address(String address) {
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
