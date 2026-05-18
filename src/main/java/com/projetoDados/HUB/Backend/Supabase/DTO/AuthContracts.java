package com.projetoDados.HUB.Backend.Supabase.DTO;

/**
 * Contratos HTTP da API de autenticação (corpo de pedido e resposta tipados).
 */
public final class AuthContracts {

    private AuthContracts() {
    }

    public record LoginRequest(String email, String password) {
    }

    public record RegisterRequest(String email, String password, String nomeEmpresa, String cnpj) {
    }

    public record AuthErrorResponse(String error) {
    }

    /** Resposta de login com tokens já extraídos do JSON do Supabase. */
    public record LoginResponse(
            String message,
            String accessToken,
            String refreshToken,
            long expiresIn,
            String tokenType) {
    }

    public record RegisterResponse(String message, String userId) {
    }

    public record MeResponse(String userId, String email, String nomeEmpresa, String cnpj) {
    }

    /** Resultado interno do serviço: ou valor tipado ou erro para mapear a HTTP. */
    public record AuthResult<T>(T value, AuthErrorResponse error) {
        public static <T> AuthResult<T> ok(T v) {
            return new AuthResult<>(v, null);
        }

        public static <T> AuthResult<T> err(String mensagem) {
            return new AuthResult<>(null, new AuthErrorResponse(mensagem));
        }

        public boolean isOk() {
            return error == null;
        }
    }
}
