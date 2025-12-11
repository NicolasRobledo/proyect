package com.example.demo.controller;

import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@CookieValue(name = "token", required = false) String token) {
        if (token == null || !jwtService.isTokenValid(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        }

        Long userId = jwtService.extractUserId(token);
        return usuarioRepository.findById(userId)
                .map(usuario -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", usuario.getId());
                    response.put("email", usuario.getEmail());
                    response.put("nombre", usuario.getNombre());
                    response.put("nombrePila", usuario.getNombrePila());
                    response.put("apellido", usuario.getApellido());
                    response.put("fotoUrl", usuario.getFotoUrl());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado")));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = "token", required = false) String token,
                                    jakarta.servlet.http.HttpServletResponse response) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("token", "");
        cookie.setHttpOnly(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
    }
}
