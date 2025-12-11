package com.example.demo.config;

import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        String googleId = oauth2User.getAttribute("sub");
        String email = oauth2User.getAttribute("email");
        Boolean emailVerified = oauth2User.getAttribute("email_verified");
        String nombre = oauth2User.getAttribute("name");
        String nombrePila = oauth2User.getAttribute("given_name");
        String apellido = oauth2User.getAttribute("family_name");
        String fotoUrl = oauth2User.getAttribute("picture");
        String locale = oauth2User.getAttribute("locale");

        Usuario usuario = usuarioRepository.findByGoogleId(googleId)
                .orElseGet(() -> {
                    Usuario nuevo = new Usuario();
                    nuevo.setGoogleId(googleId);
                    return nuevo;
                });

        usuario.setEmail(email);
        usuario.setEmailVerified(emailVerified);
        usuario.setNombre(nombre);
        usuario.setNombrePila(nombrePila);
        usuario.setApellido(apellido);
        usuario.setFotoUrl(fotoUrl);
        usuario.setLocale(locale);

        usuarioRepository.save(usuario);

        String token = jwtService.generateToken(usuario);

        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(false);
        cookie.setPath("/");
        cookie.setMaxAge(86400); // 24 hours
        response.addCookie(cookie);

        getRedirectStrategy().sendRedirect(request, response, "/");
    }
}
