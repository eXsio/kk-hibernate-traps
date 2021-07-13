package hibernate.traps.transactional_tests.dto;

import java.util.List;

public class UserDto {

    private final String name;

    private final List<String> addresses;

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
}
