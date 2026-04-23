package cl.aerosimpro.api.controller;

import cl.aerosimpro.api.dto.AuthResponseDTO;
import cl.aerosimpro.api.dto.LoginDTO;
import cl.aerosimpro.api.dto.RegisterDTO;
import cl.aerosimpro.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
