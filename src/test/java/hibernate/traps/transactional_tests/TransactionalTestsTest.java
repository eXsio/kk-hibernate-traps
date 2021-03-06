package hibernate.traps.transactional_tests;

import com.google.gson.Gson;
import hibernate.traps.transactional_tests.config.TransactionalTestsConfig;
import hibernate.traps.transactional_tests.dto.UserDto;
import hibernate.traps.transactional_tests.util.TestDatabaseUtil;
import org.assertj.core.util.Lists;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TransactionalTestsConfig.class})
@WebMvcTest
public class TransactionalTestsTest {

    private static final String USER_NAME = "userName";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @Test
    @Transactional
    public void webIntegrationTestWithNotActualProductionTransactionScope() throws Exception {
        //GIVEN: There is a User created
        createNewUser(getNewUser());

        //WHEN: we try to fetch the User by name, including its lazy-loaded properties
        MvcResult createdUserResponse = getUserByName(USER_NAME);

        //THEN: unlike in production, no exception is thrown when trying to access lazy-initialized properties
        assertEquals(200, createdUserResponse.getResponse().getStatus());
        UserDto createdUser = getUserFromResponse(createdUserResponse);
        assertEquals(USER_NAME, createdUser.getName());
        assertEquals(2, createdUser.getAddresses().size());
    }

    @Test
    public void webIntegrationTestWithActualProductionTransactionScope() throws Exception {
        //GIVEN: There is a User created
        createNewUser(getNewUser());

        //WHEN: we try to fetch the User by name, including its lazy-loaded properties
        MvcResult createdUserResponse = getUserByName(USER_NAME);

        //THEN: just like in production, an exception is thrown when trying to access lazy-initialized properties
        assertEquals(500, createdUserResponse.getResponse().getStatus());
        assertEquals(LazyInitializationException.class, createdUserResponse.getResolvedException().getClass());

        //CLEANUP: we can't rely on Spring to roll back our test, we need to cleanup the Test Database manually
        TestDatabaseUtil.resetDatabase(dataSource);
    }

    private void createNewUser(UserDto user) throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(user))
        )
                .andExpect(status().isCreated())
                .andReturn();
    }

    private MvcResult getUserByName(String userName) throws Exception {
        return mockMvc.perform(get("/users/" + userName)
                .accept(MediaType.APPLICATION_JSON)
        ).andReturn();

    }

    private UserDto getUserFromResponse(MvcResult response) throws Exception {
        return new Gson().fromJson(response.getResponse().getContentAsString(), UserDto.class);
    }

    private UserDto getNewUser() {
        return new UserDto(USER_NAME, Lists.newArrayList("a1", "a2"));
    }
}
