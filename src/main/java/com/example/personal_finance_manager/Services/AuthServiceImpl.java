package com.example.personal_finance_manager.Services;

import com.example.personal_finance_manager.DTOs.AuthRegisterResponseDTO;
import com.example.personal_finance_manager.DTOs.AuthRequestDTO;
import com.example.personal_finance_manager.DTOs.UsuarioRequestDTO;
import com.example.personal_finance_manager.DTOs.UsuarioResponseDTO;
import com.example.personal_finance_manager.Exceptions.BadRequestException;
import com.example.personal_finance_manager.Models.Usuario;
import com.example.personal_finance_manager.Security.JwtUtils;
import com.example.personal_finance_manager.Security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService{

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthServiceImpl(UsuarioService usuarioService, AuthenticationManager authenticationManager, JwtUtils jwtUtils){
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public AuthRegisterResponseDTO login(AuthRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String jwt = jwtUtils.generateToken(userDetails);

            Usuario usuario = usuarioService.obtenerUsuarioPorEmail(userDetails.getEmail());
            UsuarioResponseDTO usuarioResponseDTO = usuarioService.aUsuarioResponseDTO(usuario);
            return new AuthRegisterResponseDTO(jwt, usuarioResponseDTO);
        } catch (AuthenticationException e) {
            throw new BadRequestException("Credenciales inválidas");
        }
    }


    public AuthRegisterResponseDTO register(UsuarioRequestDTO request) {
        UsuarioResponseDTO usuarioCreado = usuarioService.crearUsuario(request);
        request.setId(usuarioCreado.getId());
        UserDetailsImpl userDetails = UserDetailsImpl.aUserDetailsImpl(usuarioService.aUsuarioEntity(request));

        String jwt = jwtUtils.generateToken(userDetails);

        return new AuthRegisterResponseDTO(jwt, usuarioCreado);
    }

    public Usuario obtenerUsuarioAutenticado(Authentication authentication){
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return usuarioService.obtenerUsuarioPorEmail(userDetails.getEmail());
    }
}
