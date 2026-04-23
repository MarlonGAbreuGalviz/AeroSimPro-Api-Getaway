package cl.aerosimpro.api.service.impl;

import cl.aerosimpro.api.dto.PersonaResponseDTO;
import cl.aerosimpro.api.dto.AuthResponseDTO;
import cl.aerosimpro.api.dto.LoginDTO;
import cl.aerosimpro.api.dto.RegisterDTO;
import cl.aerosimpro.api.model.PersonaModel;
import cl.aerosimpro.api.model.SessionToken;
import cl.aerosimpro.api.repository.PersonaRepository;
import cl.aerosimpro.api.repository.SessionTokenRepository;
import cl.aerosimpro.api.security.TokenGenerator;
import cl.aerosimpro.api.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PersonaRepository personaRepository;
    private final SessionTokenRepository sessionTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGenerator tokenGenerator;

    @Value("${app.token.expiration-hours}")
    private long tokenExpirationHours;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterDTO request) {
        String email = request.getEmail().trim().toLowerCase();

        if (personaRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }

        PersonaModel persona = PersonaModel.builder()
                .nombre(request.getNombre().trim())
                .apellido(request.getApellido().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role("PERSONA")
                .build();

        PersonaModel savedPersona = personaRepository.save(persona);

        String token = createAndSaveToken(savedPersona);

        return AuthResponseDTO.builder()
                .token(token)
                .persona(toPersonaResponse(savedPersona))
                .build();
    }

    @Override
    @Transactional
    public AuthResponseDTO login(LoginDTO request) {
        String email = request.getUsername().trim().toLowerCase();

        PersonaModel persona = personaRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), persona.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        deactivateActiveTokens(persona);

        String token = createAndSaveToken(persona);

        return AuthResponseDTO.builder()
                .token(token)
                .persona(toPersonaResponse(persona))
                .build();
    }

    private String createAndSaveToken(PersonaModel persona) {
        String tokenValue = tokenGenerator.generateToken();

        SessionToken sessionToken = SessionToken.builder()
                .token(tokenValue)
                .persona(persona)
                .expirationDate(LocalDateTime.now().plusHours(tokenExpirationHours))
                .active(true)
                .build();

        sessionTokenRepository.save(sessionToken);

        return tokenValue;
    }

    private void deactivateActiveTokens(PersonaModel persona) {
        List<SessionToken> activeTokens = sessionTokenRepository.findAllByPersonaAndActiveTrue(persona);

        for (SessionToken token : activeTokens) {
            token.setActive(false);
        }

        sessionTokenRepository.saveAll(activeTokens);
    }

    private PersonaResponseDTO toPersonaResponse(PersonaModel persona) {
        return PersonaResponseDTO.builder()
                .id(persona.getId())
                .nombre(persona.getNombre())
                .apellido(persona.getApellido())
                .email(persona.getEmail())
                .role(persona.getRole())
                .build();
    }
}