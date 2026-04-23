package cl.aerosimpro.api.repository;

import cl.aerosimpro.api.model.PersonaModel;
import cl.aerosimpro.api.model.SessionToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionTokenRepository extends JpaRepository<SessionToken, Long> {

    Optional<SessionToken> findByTokenAndActiveTrue(String token);

    List<SessionToken> findAllByPersonaAndActiveTrue(PersonaModel persona);
}