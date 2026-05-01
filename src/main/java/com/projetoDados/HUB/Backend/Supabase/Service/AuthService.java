package com.projetoDados.HUB.Backend.Supabase.Service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AuthService {

    private final RestClient restClient;
    private final String supabaseKey;

    public AuthService(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.key}") String supabaseKey) {
        this.supabaseKey = supabaseKey;
        this.restClient = RestClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader("apikey", supabaseKey)
                .build();
    }

    // Cadastra um novo usuario no Supabase e salva os dados da empresa
    public Map<String, Object> register(String email, String password, String nomeEmpresa, String cnpj) {
        try {
            Map<String, String> body = Map.of("email", email, "password", password);

            String response = restClient.post()
                    .uri("/auth/v1/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            // pega o id do usuario criado
            String userId = pegarCampo(response, "id");

            // salva empresa na tabela do supabase
            if (nomeEmpresa != null && cnpj != null) {
                salvarEmpresa(userId, nomeEmpresa, cnpj, email);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Cadastro realizado com sucesso");
            result.put("data", response);
            return result;

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Erro ao cadastrar: " + e.getMessage());
            return error;
        }
    }

    // Faz login e retorna o token
    public Map<String, Object> login(String email, String password) {
        try {
            Map<String, String> body = Map.of("email", email, "password", password);

            String response = restClient.post()
                    .uri("/auth/v1/token?grant_type=password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Login realizado com sucesso");
            result.put("data", response);
            return result;

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Email ou senha incorretos");
            return error;
        }
    }

    // Retorna os dados do usuario logado a partir do token
    public Map<String, Object> getUser(String token) {
        try {
            String response = restClient.get()
                    .uri("/auth/v1/user")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(String.class);

            Map<String, Object> result = new HashMap<>();
            result.put("user_id", pegarCampo(response, "id"));
            result.put("email", pegarCampo(response, "email"));
            return result;

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Token invalido");
            return error;
        }
    }

    // Salva os dados da empresa na tabela "empresas" do Supabase
    private void salvarEmpresa(String userId, String nomeEmpresa, String cnpj, String email) {
        try {
            String json = String.format(
                    "{\"user_id\":\"%s\",\"nome_empresa\":\"%s\",\"cnpj\":\"%s\",\"email\":\"%s\"}",
                    userId, nomeEmpresa, cnpj, email);

            restClient.post()
                    .uri("/rest/v1/empresas")
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header("Prefer", "return=representation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .retrieve()
                    .body(String.class);

            System.out.println("Empresa salva!");
        } catch (Exception e) {
            System.out.println("Erro ao salvar empresa: " + e.getMessage());
        }
    }

    // Pega um campo simples de um JSON (ex: "id", "email")
    private String pegarCampo(String json, String campo) {
        try {
            String chave = "\"" + campo + "\":\"";
            int inicio = json.indexOf(chave) + chave.length();
            int fim = json.indexOf("\"", inicio);
            return json.substring(inicio, fim);
        } catch (Exception e) {
            return null;
        }
    }
}
