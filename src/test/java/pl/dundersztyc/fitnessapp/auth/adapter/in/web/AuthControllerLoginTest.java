package pl.dundersztyc.fitnessapp.auth.adapter.in.web;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import pl.dundersztyc.fitnessapp.AbstractIntegrationTest;
import pl.dundersztyc.fitnessapp.auth.application.port.in.AuthRequest;
import pl.dundersztyc.fitnessapp.user.application.port.out.SaveUserPort;
import pl.dundersztyc.fitnessapp.user.domain.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.dundersztyc.fitnessapp.common.JsonMapper.fromJson;
import static pl.dundersztyc.fitnessapp.common.JsonMapper.toJsonString;
import static pl.dundersztyc.fitnessapp.common.UserTestData.defaultUser;


class AuthControllerLoginTest extends AbstractIntegrationTest {

    @Autowired
    private SaveUserPort saveUserPort;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String LOGIN_URL = "/api/v1/public/login";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @Test
    public void shouldLoginPass() throws Exception {

        saveUser(USERNAME, PASSWORD);

        AuthRequest authRequest = new AuthRequest(USERNAME, PASSWORD);

        MvcResult loginResult = login(authRequest)
            .andExpect(status().isOk())
            .andExpect(header().exists(HttpHeaders.AUTHORIZATION))
            .andReturn();

        UserView userView = fromJson(loginResult.getResponse().getContentAsString(), UserView.class);

        assertThat(userView.username()).isEqualTo(authRequest.username());
    }

    @Test
    public void usernameIsIncorrect() throws Exception {

        saveUser(USERNAME, PASSWORD);
        AuthRequest authRequest = new AuthRequest("incorrectUsername", PASSWORD);

        login(authRequest)
            .andExpect(status().isUnauthorized())
            .andExpect(header().doesNotExist(HttpHeaders.AUTHORIZATION))
            .andExpect(content().string(Matchers.blankString()));
    }

    @Test
    public void passwordIsIncorrect() throws Exception {

        saveUser(USERNAME, PASSWORD);
        AuthRequest authRequest = new AuthRequest(USERNAME, "incorrectPassword");

        login(authRequest)
            .andExpect(status().isUnauthorized())
            .andExpect(header().doesNotExist(HttpHeaders.AUTHORIZATION))
            .andExpect(content().string(Matchers.blankString()));
    }

    private ResultActions login(AuthRequest authRequest) throws Exception {
        return mockMvc
                .perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(authRequest)));
    }

    private void saveUser(String username, String password) {
        User user = defaultUser()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build();

        saveUserPort.save(user);
    }

}