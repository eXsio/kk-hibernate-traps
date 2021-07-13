package hibernate.traps.transactional_tests.controller;

import hibernate.traps.transactional_tests.dto.UserDto;
import hibernate.traps.transactional_tests.model.Address;
import hibernate.traps.transactional_tests.model.User;
import hibernate.traps.transactional_tests.service.UserService;
import org.junit.platform.commons.util.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createUser(@RequestBody UserDto user) {
        userService.createUser(user);
    }

    @GetMapping("/{name}")
    public UserDto getUserByName(@PathVariable("name") String name) {
        User user = userService.getUserByName(name).orElseThrow(() -> new RuntimeException("User not Found"));
        return new UserDto(user.getName(), user.getAddresses().stream().map(Address::getAddress).collect(Collectors.toList()));
    }

    @ExceptionHandler(RuntimeException.class)
    public final ResponseEntity<Exception> handleAllExceptions(RuntimeException ex) {
        return new ResponseEntity<>(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
