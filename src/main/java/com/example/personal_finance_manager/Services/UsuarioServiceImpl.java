package com.example.personal_finance_manager.Services;

import com.example.personal_finance_manager.DTOs.UsuarioRequestDTO;
import com.example.personal_finance_manager.DTOs.UsuarioResponseDTO;
import com.example.personal_finance_manager.DTOs.UsuarioUpdateDTO;
import com.example.personal_finance_manager.Exceptions.BadRequestException;
import com.example.personal_finance_manager.Exceptions.NotFoundException;
import com.example.personal_finance_manager.Models.Usuario;
import com.example.personal_finance_manager.Repositories.UsuarioRepository;
import com.example.personal_finance_manager.Security.UserDetailsImpl;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Primary
@Transactional
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public void setUsuarioRepository(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder){
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findUsuarioByEmail(email);
    }

    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO usuarioRequestDTO){
        boolean isExistUsuario = usuarioRepository.existsByEmail(usuarioRequestDTO.getEmail());
        if(isExistUsuario){
           throw new BadRequestException("Usuario ya registrado");
        }
        Usuario usuario = usuarioRepository.save(aUsuarioEntity(usuarioRequestDTO));
        return aUsuarioResponseDTO(usuario);
    }

    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioUpdateDTO dto){
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        usuario.setNombre(dto.getNombre());

        if (dto.getNuevaPassword() != null && !dto.getNuevaPassword().isBlank()) {
            if (dto.getPasswordActual() == null || !passwordEncoder.matches(dto.getPasswordActual(), usuario.getPassword())) {
                throw new BadRequestException("La contraseña actual es incorrecta");
            }
            usuario.setPassword(passwordEncoder.encode(dto.getNuevaPassword()));
        }

        usuarioRepository.save(usuario);
        return aUsuarioResponseDTO(usuario);
    }
    public UsuarioResponseDTO aUsuarioResponseDTO(Usuario usuario) {
        UsuarioResponseDTO usuarioResponseDTO = new UsuarioResponseDTO();

        usuarioResponseDTO.setId(usuario.getId());
        usuarioResponseDTO.setEmail(usuario.getEmail());
        usuarioResponseDTO.setNombre(usuario.getNombre());
        usuarioResponseDTO.setRol(usuario.getRol());

        return usuarioResponseDTO;
    }

    public Usuario aUsuarioEntity(UsuarioRequestDTO usuarioRequestDTO){
        Usuario usuario = new Usuario();

        usuario.setEmail(usuarioRequestDTO.getEmail());
        usuario.setNombre(usuarioRequestDTO.getNombre());
        usuario.setPassword(passwordEncoder.encode(usuarioRequestDTO.getPassword()));
        usuario.setRol(usuarioRequestDTO.getRol());

        return usuario;
    }

    public UsuarioResponseDTO aUsuarioResponseDTOFromUserDetails(UserDetailsImpl userDetails) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(userDetails.getId());
        dto.setEmail(userDetails.getEmail());
        dto.setNombre(userDetails.getNombre());
        dto.setRol(userDetails.getRol());
        return dto;
    }
}
