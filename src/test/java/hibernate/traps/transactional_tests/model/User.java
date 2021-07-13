package hibernate.traps.transactional_tests.model;

import org.assertj.core.util.Sets;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "APP_USERS")
public class User {

    @Id
    @Column(name = "USER_ID", nullable = false)
    @GeneratedValue
    private Long id;

    @Column(name = "NAME", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Address> addresses = Sets.newHashSet();

    public User() {
    }

    public User(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(Set<Address> addresses) {
        this.addresses = addresses;
    }

    public void addAddress(Address address) {
        addresses.add(address);
        address.setUser(this);
    }
}
