package hibernate.traps.transactional_tests.dto;

import java.util.List;

public class UserDto {

    private String name;

    private List<String> addresses;

    public UserDto() {
    }

    public UserDto(String name, List<String> addresses) {
        this.name = name;
        this.addresses = addresses;
    }

    public String getName() {
        return name;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }
}
