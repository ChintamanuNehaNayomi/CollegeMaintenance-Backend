package com.example.project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.example.project.service.JwtService;
import com.example.project.entity.User;
import com.example.project.repository.UserRepository;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private EntityManager entityManager;

	@DynamicPropertySource
	static void jwtProperties(DynamicPropertyRegistry registry) {
		registry.add(
				"app.jwt.secret",
				() -> UUID.randomUUID().toString()
						+ UUID.randomUUID().toString());
	}

	@Test
	void contextLoads() {
	}

	@Test
	void adminEndpointsRejectAnonymousAndNonAdminUsers() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/staff"))
				.andExpect(status().isUnauthorized());

		String studentToken = jwtService.generateAccessToken(
				"student1", "STUDENT");
		mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/staff")
				.header("Authorization", "Bearer " + studentToken))
				.andExpect(status().isForbidden());
	}

	@Test
	void corsAllowsConfiguredVercelOrigin() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.options("/api/login")
				.header("Origin",
						"https://collegemaintenance-fronted.vercel.app")
				.header("Access-Control-Request-Method", "POST"))
				.andExpect(status().isOk())
				.andExpect(header().string(
						"Access-Control-Allow-Origin",
						"https://collegemaintenance-fronted.vercel.app"));
	}

	@Test
	@Transactional
	void registrationPersistsBcryptPasswordAndAllowsFormEncodedLogin()
			throws Exception {
		String username = "student-" + UUID.randomUUID();
		String password = "test-password-without-production-data";
		String registration = """
				{"name":"Test Student","username":"  %s  ",
				 "email":"%s@example.test","password":"%s","role":"ADMIN"}
				""".formatted(username, username, password);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(registration))
				.andExpect(status().isCreated());

		userRepository.flush();
		entityManager.clear();
		User savedUser = userRepository.findByUsernameIgnoreCase(username).orElseThrow();
		org.junit.jupiter.api.Assertions.assertEquals(
				"STUDENT", savedUser.getRole());
		org.junit.jupiter.api.Assertions.assertNotEquals(
				password, savedUser.getPassword());
		org.junit.jupiter.api.Assertions.assertTrue(
				passwordEncoder.matches(password, savedUser.getPassword()));

		MvcResult loginResult = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/login")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("username", " " + username + " ")
						.param("password", password))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value(username))
				.andExpect(jsonPath("$.role").value("STUDENT"))
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.refreshToken").isNotEmpty())
				.andReturn();

		org.junit.jupiter.api.Assertions.assertFalse(
				loginResult.getResponse().getContentAsString().contains(password));
	}

	@Test
	@Transactional
	void loginRejectsAnIncorrectPassword() throws Exception {
		String username = "student-" + UUID.randomUUID();
		User user = new User();
		user.setUsername(username);
		user.setPassword(passwordEncoder.encode("correct-test-password"));
		user.setRole("STUDENT");
		userRepository.save(user);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("username", username)
				.param("password", "incorrect-test-password"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@Transactional
	void loginTrimsAndIgnoresUsernameCaseAndMeReturnsDatabaseRole()
			throws Exception {
		String username = "Student-" + UUID.randomUUID();
		String password = "case-test-password";
		User user = new User();
		user.setUsername(username);
		user.setPassword(passwordEncoder.encode(password));
		user.setRole("STUDENT");
		userRepository.save(user);

		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/login")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("username", "  " + username.toLowerCase() + "  ")
						.param("password", password))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("STUDENT"));

		String token = jwtService.generateAccessToken(username, "STUDENT");
		mockMvc.perform(MockMvcRequestBuilders.get("/api/me")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value(username))
				.andExpect(jsonPath("$.role").value("STUDENT"));
	}
}
