package com.projetoDados.HUB.Backend.Supabase.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projetoDados.HUB.Backend.Supabase.DTO.AuthContracts.LoginRequest;
import com.projetoDados.HUB.Backend.Supabase.DTO.AuthContracts.RegisterRequest;
import com.projetoDados.HUB.Backend.Supabase.Service.AuthService;

import lombok.RequiredArgsConstructor;

/**
 * Autenticação via Supabase: registro, login e {@code /user} com Bearer token.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest body) {
        var r = authService.register(
                body.email(),
                body.password(),
                body.nomeEmpresa(),
                body.cnpj());
        if (!r.isOk()) {
            return ResponseEntity.badRequest().body(r.error());
        }
        return ResponseEntity.ok(r.value());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest body) {
        var r = authService.login(body.email(), body.password());
        if (!r.isOk()) {
            return ResponseEntity.status(401).body(r.error());
        }
        return ResponseEntity.ok(r.value());
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        var r = authService.getUser(token);
        if (!r.isOk()) {
            return ResponseEntity.status(401).body(r.error());
        }
        return ResponseEntity.ok(r.value());
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody java.util.Map<String, String> body) {
        String token = authHeader.replace("Bearer ", "").trim();
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Senha deve ter pelo menos 6 caracteres."));
        }
        var r = authService.changePassword(token, newPassword);
        if (!r.isOk()) {
            return ResponseEntity.badRequest().body(r.error());
        }
        return ResponseEntity.ok(java.util.Map.of("message", "Senha alterada com sucesso."));
    }
}
