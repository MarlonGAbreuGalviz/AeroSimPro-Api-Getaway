package cl.aerosimpro.api.security;

import cl.aerosimpro.api.model.PersonaModel;
import cl.aerosimpro.api.model.SessionToken;
import cl.aerosimpro.api.repository.SessionTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RandomTokenAuthentication extends OncePerRequestFilter {

    private final SessionTokenRepository sessionTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String tokenValue = authHeader.substring(7).trim();

        Optional<SessionToken> optionalSessionToken = sessionTokenRepository.findByTokenAndActiveTrue(tokenValue);

        if (optionalSessionToken.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        SessionToken sessionToken = optionalSessionToken.get();

        if (sessionToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            sessionToken.setActive(false);
            sessionTokenRepository.save(sessionToken);
            filterChain.doFilter(request, response);
            return;
        }

        PersonaModel agent = sessionToken.getPersona();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        agent.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + agent.getRole()))
                );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}