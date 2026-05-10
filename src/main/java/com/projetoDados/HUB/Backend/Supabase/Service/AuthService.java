package com.projetoDados.HUB.Backend.Supabase.Service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.projetoDados.HUB.Backend.Supabase.DTO.AuthContracts.AuthResult;
import com.projetoDados.HUB.Backend.Supabase.DTO.AuthContracts.LoginResponse;
import com.projetoDados.HUB.Backend.Supabase.DTO.AuthContracts.MeResponse;
import com.projetoDados.HUB.Backend.Supabase.DTO.AuthContracts.RegisterResponse;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Integração com Supabase Auth (signup, password grant) e REST {@code /rest/v1/empresas}.
 * <p>
 * <b>O que entrega:</b> tokens e ids já parseados (sem string JSON opaca para o front), ou mensagens de erro
 * estáveis para o cliente HTTP. Usa {@link JsonMapper} (Jackson 3, padrão no Spring Boot 4).
 */
@Slf4j
@Service
public class AuthService {

    private final RestClient restClient;
    private final String supabaseKey;
    private final JsonMapper jsonMapper;

    public AuthService(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.key}") String supabaseKey,
            JsonMapper jsonMapper) {
        this.supabaseKey = supabaseKey;
        this.jsonMapper = jsonMapper;
        this.restClient = RestClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader("apikey", supabaseKey)
                .build();
    }

    public AuthResult<RegisterResponse> register(
            String email,
            String password,
            String nomeEmpresa,
            String cnpj) {
        try {
            String bodyJson = jsonMapper.writeValueAsString(
                    Map.of("email", nuloSeguro(email), "password", nuloSeguro(password)));

            String response = restClient.post()
                    .uri("/auth/v1/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(bodyJson)
                    .retrieve()
                    .body(String.class);

            JsonNode root = jsonMapper.readTree(response);
            String userId = extrairUserIdSignup(root);

            if (nomeEmpresa != null && cnpj != null && userId != null) {
                salvarEmpresa(userId, nomeEmpresa, cnpj, email);
            }

            return AuthResult.ok(new RegisterResponse("Cadastro realizado com sucesso", userId));
        } catch (RestClientException e) {
            log.warn("Falha HTTP no signup: {}", e.getMessage());
            return AuthResult.err("Erro ao cadastrar (email=" + email + "). Verifique os dados ou o Supabase.");
        } catch (Exception e) {
            log.warn("Falha no signup: {}", e.getMessage());
            return AuthResult.err(buildCadastroErrorMessage(email, e));
        }
    }

    public AuthResult<LoginResponse> login(String email, String password) {
        try {
            String bodyJson = jsonMapper.writeValueAsString(
                    Map.of("email", nuloSeguro(email), "password", nuloSeguro(password)));

            String response = restClient.post()
                    .uri("/auth/v1/token?grant_type=password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(bodyJson)
                    .retrieve()
                    .body(String.class);

            JsonNode root = jsonMapper.readTree(response);
            String access = texto(root, "access_token");
            if (access == null || access.isBlank()) {
                return AuthResult.err("Email ou senha incorretos");
            }
            String refresh = texto(root, "refresh_token");
            long exp = root.path("expires_in").asLong(0L);
            String typ = texto(root, "token_type");
            if (typ == null) {
                typ = "bearer";
            }

            return AuthResult.ok(new LoginResponse(
                    "Login realizado com sucesso",
                    access,
                    refresh != null ? refresh : "",
                    exp,
                    typ));
        } catch (RestClientException e) {
            log.debug("Login recusado: {}", e.getMessage());
            return AuthResult.err("Email ou senha incorretos");
        } catch (Exception e) {
            log.warn("Erro inesperado no login: {}", e.getMessage());
            return AuthResult.err("Email ou senha incorretos");
        }
    }

    public AuthResult<MeResponse> getUser(String bearerToken) {
        try {
            String response = restClient.get()
                    .uri("/auth/v1/user")
                    .header("Authorization", "Bearer " + bearerToken)
                    .retrieve()
                    .body(String.class);

            JsonNode root = jsonMapper.readTree(response);
            String id = texto(root, "id");
            String mail = texto(root, "email");
            if (id == null) {
                return AuthResult.err("Token invalido");
            }
            return AuthResult.ok(new MeResponse(id, mail != null ? mail : ""));
        } catch (Exception e) {
            log.debug("Token invalido ou expirado: {}", e.getMessage());
            return AuthResult.err("Token invalido");
        }
    }

    private void salvarEmpresa(String userId, String nomeEmpresa, String cnpj, String email) {
        try {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("user_id", userId);
            row.put("nome_empresa", nomeEmpresa);
            row.put("cnpj", cnpj);
            row.put("email", email != null ? email : "");

            String json = jsonMapper.writeValueAsString(row);

            restClient.post()
                    .uri("/rest/v1/empresas")
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header("Prefer", "return=representation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .retrieve()
                    .body(String.class);

            log.info("Empresa associada ao user_id={}", userId);
        } catch (Exception e) {
            log.warn("Nao foi possivel gravar empresas para user_id={}: {}", userId, e.getMessage());
        }
    }

    private static String extrairUserIdSignup(JsonNode root) {
        String id = texto(root, "id");
        if (id != null) {
            return id;
        }
        JsonNode user = root.get("user");
        if (user != null && user.isObject()) {
            return texto(user, "id");
        }
        return null;
    }

    private static String texto(JsonNode n, String campo) {
        JsonNode v = n.get(campo);
        if (v == null || v.isNull()) {
            return null;
        }
        return v.isValueNode() ? v.stringValue() : null;
    }

    private static String nuloSeguro(String s) {
        return s != null ? s : "";
    }

    private static String buildCadastroErrorMessage(String email, Exception e) {
        return "Erro ao cadastrar (email=" + email + "). Excecao=" + e.getClass().getName()
                + ", mensagem=" + e.getMessage();
    }
}

