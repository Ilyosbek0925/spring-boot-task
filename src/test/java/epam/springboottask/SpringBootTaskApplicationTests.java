package epam.springboottask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import epam.springboottask.entity.TaskItem;
import epam.springboottask.entity.UserAccount;
import epam.springboottask.repository.TaskRepository;
import epam.springboottask.repository.UserAccountRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SpringBootTaskApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void repositoryPersistsTasks() {
        UserAccount owner = userAccountRepository.save(UserAccount.builder()
                .username("repository-" + UUID.randomUUID())
                .passwordHash(passwordEncoder.encode("repository-test-password"))
                .role("USER")
                .build());
        TaskItem saved = taskRepository.save(TaskItem.builder()
                .title("Repository test")
                .description("Persisted")
                .completed(false)
                .owner(owner)
                .build());

        assertEquals(saved.getId(), taskRepository.findById(saved.getId()).orElseThrow().getId());

        taskRepository.delete(saved);
    }

    @Test
    void registersUsersIssuesJwtAndAllowsAuthenticatedCrud() throws Exception {
        String username = "user-" + UUID.randomUUID();
        String credentials = """
                {"username":"%s","password":"a-secure-password"}
                """.formatted(username);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();
        JsonNode login = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = login.get("accessToken").asText();

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/status").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        String adminUsername = "admin-" + UUID.randomUUID();
        userAccountRepository.save(UserAccount.builder()
                .username(adminUsername)
                .passwordHash(passwordEncoder.encode("an-admin-password"))
                .role("ADMIN")
                .build());
        MvcResult adminLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"an-admin-password"}
                                """.formatted(adminUsername)))
                .andExpect(status().isOk())
                .andReturn();
        String adminToken = objectMapper.readTree(adminLoginResult.getResponse().getContentAsString())
                .get("accessToken").asText();
        mockMvc.perform(get("/api/admin/status").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("admin access granted"));

        MvcResult createResult = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"First task","description":"Created in the integration test","completed":false}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("First task"))
                .andExpect(jsonPath("$.ownerUsername").value(username))
                .andReturn();
        long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        String otherUsername = "other-" + UUID.randomUUID();
        String otherCredentials = """
                {"username":"%s","password":"another-secure-password"}
                """.formatted(otherUsername);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(otherCredentials))
                .andExpect(status().isCreated());
        MvcResult otherLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(otherCredentials))
                .andExpect(status().isOk())
                .andReturn();
        String otherToken = objectMapper.readTree(otherLoginResult.getResponse().getContentAsString())
                .get("accessToken").asText();
        mockMvc.perform(get("/api/tasks/{id}", id).header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));

        mockMvc.perform(get("/actuator/metrics/tasks.created")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/tasks/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
        mockMvc.perform(put("/api/tasks/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Updated task","description":"Updated","completed":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
        mockMvc.perform(delete("/api/tasks/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/tasks/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsConsistentUnauthorizedErrorResponse() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }

    @Test
    void validatesTaskAndCredentialInputs() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"invalid","password":"short"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.fieldErrors.password").exists());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"missing","password":"a-secure-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }
}
