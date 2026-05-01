package com.projetoDados.HUB.Backend.Supabase.Controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.projetoDados.HUB.Backend.Supabase.Service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /auth/register - cria conta nova
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        String nomeEmpresa = body.get("nomeEmpresa");
        String cnpj = body.get("cnpj");

        Map<String, Object> result = authService.register(email, password, nomeEmpresa, cnpj);

        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    // POST /auth/login - faz login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        Map<String, Object> result = authService.login(email, password);

        if (result.containsKey("error")) {
            return ResponseEntity.status(401).body(result);
        }
        return ResponseEntity.ok(result);
    }

    // GET /auth/me - retorna o id e email do usuario logado
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Map<String, Object> result = authService.getUser(token);

        if (result.containsKey("error")) {
            return ResponseEntity.status(401).body(result);
        }
        return ResponseEntity.ok(result);
    }
}
