package com.parquecafe.accesoapi.controller;

import com.parquecafe.accesoapi.dto.LoginRequest;
import com.parquecafe.accesoapi.dto.LoginResponse;
import com.parquecafe.accesoapi.model.Usuario;
import com.parquecafe.accesoapi.repository.UsuarioRepository;
import com.parquecafe.accesoapi.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {
        "http://localhost:4200",
        "https://front-sistema-de-control-de-acceso.vercel.app"
})
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(request.getUsername());

        if (usuarioOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), usuarioOpt.get().getPassword())) {
            throw new IllegalArgumentException("Usuario o contraseña incorrectos");
        }

        Usuario usuario = usuarioOpt.get();
        String token = jwtService.generarToken(usuario.getUsername(), usuario.getRol().name());

        return new LoginResponse(token, usuario.getRol().name(), usuario.getNombreMostrar());
    }
}