package cl.aerosimpro.api.repository;

import cl.aerosimpro.api.model.PersonaModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonaRepository extends JpaRepository<PersonaModel, Long> {

    Optional<PersonaModel> findByEmail(String email);

    boolean existsByEmail(String email);
}