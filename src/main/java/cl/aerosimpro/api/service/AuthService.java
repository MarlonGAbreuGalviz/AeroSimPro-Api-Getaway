package cl.aerosimpro.api.service;

import cl.aerosimpro.api.dto.AuthResponseDTO;
import cl.aerosimpro.api.dto.LoginDTO;
import cl.aerosimpro.api.dto.RegisterDTO;

public interface AuthService {

    AuthResponseDTO register(RegisterDTO request);

    AuthResponseDTO login(LoginDTO request);
}