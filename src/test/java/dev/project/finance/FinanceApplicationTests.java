package dev.project.finance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FinanceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerSempreCriaUsuarioComRoleUsuario() throws Exception {
        Map<String, Object> register = new HashMap<>();
        register.put("nome", "User Base");
        register.put("email", "base@example.com");
        register.put("senha", "senha12345");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USUARIO"));
    }

    @Test
    void loginInvalidoRetorna401() throws Exception {
        Map<String, Object> login = new HashMap<>();
        login.put("email", "naoexiste@example.com");
        login.put("senha", "senhaErrada");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshTokenRotacionaEInvalidaTokenAnterior() throws Exception {
        register("refresh@example.com", "Senha12345");
        JsonNode login = login("refresh@example.com", "Senha12345");
        String refreshToken = login.get("refreshToken").asText();

        Map<String, Object> refreshPayload = Map.of("refreshToken", refreshToken);
        String refreshResult = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode refreshResponse = objectMapper.readTree(refreshResult);
        String novoRefreshToken = refreshResponse.get("refreshToken").asText();

        Map<String, Object> oldTokenPayload = Map.of("refreshToken", refreshToken);
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oldTokenPayload)))
                .andExpect(status().isUnauthorized());

        Map<String, Object> newTokenPayload = Map.of("refreshToken", novoRefreshToken);
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTokenPayload)))
                .andExpect(status().isOk());
    }

    @Test
    void endpointProtegidoSemTokenRetorna401() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioNaoAcessaContaDeOutroUsuario() throws Exception {
        register("owner@example.com", "Senha12345");
        register("other@example.com", "Senha12345");
        String ownerToken = login("owner@example.com", "Senha12345").get("accessToken").asText();
        String otherToken = login("other@example.com", "Senha12345").get("accessToken").asText();

        Long accountId = createAccount(ownerToken, "Carteira", "DINHEIRO", new BigDecimal("100.00"));

        mockMvc.perform(get("/accounts/{id}", accountId)
                .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void naoPermiteCriarTransacaoEmContaDesativada() throws Exception {
        register("inactive-account@example.com", "Senha12345");
        String accessToken = login("inactive-account@example.com", "Senha12345").get("accessToken").asText();
        Long accountId = createAccount(accessToken, "Conta Inativa", "DINHEIRO", new BigDecimal("200.00"));

        mockMvc.perform(delete("/accounts/{id}", accountId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("accountId", accountId);
        transaction.put("tipo", "RECEITA");
        transaction.put("valor", new BigDecimal("50.00"));
        transaction.put("descricao", "Recebimento");
        transaction.put("dataTransacao", LocalDate.now().toString());

        mockMvc.perform(post("/transactions")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(status().isNotFound());
    }

    @Test
    void tipoTransacaoInvalidoRetorna400() throws Exception {
        register("invalid-tipo@example.com", "Senha12345");
        String accessToken = login("invalid-tipo@example.com", "Senha12345").get("accessToken").asText();
        Long accountId = createAccount(accessToken, "Conta Teste", "DINHEIRO", new BigDecimal("100.00"));

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("accountId", accountId);
        transaction.put("tipo", "entrada");
        transaction.put("valor", new BigDecimal("10.00"));
        transaction.put("descricao", "Descricao");
        transaction.put("dataTransacao", LocalDate.now().toString());

        mockMvc.perform(post("/transactions")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tipo de transacao invalido. Valores aceitos: RECEITA ou DESPESA"));
    }
    private void register(String email, String senha) throws Exception {
        Map<String, Object> register = new HashMap<>();
        register.put("nome", "Teste");
        register.put("email", email);
        register.put("senha", senha);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());
    }

    private JsonNode login(String email, String senha) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("senha", senha);

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response);
    }

    private Long createAccount(String accessToken, String nome, String tipo, BigDecimal saldo) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("nome", nome);
        payload.put("tipo", tipo);
        payload.put("saldoInicial", saldo);

        String response = mockMvc.perform(post("/accounts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }
}
