package cl.aerosimpro.api.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaResponseDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String role;
}