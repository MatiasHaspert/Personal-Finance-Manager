package com.example.personal_finance_manager.Security;

import com.example.personal_finance_manager.Models.Usuario;
import com.example.personal_finance_manager.Repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private UsuarioRepository usuarioRepository;

    @Autowired
    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository){
        this.usuarioRepository = usuarioRepository;
    }

    public UserDetails loadUserByUsername(String email) {
        Usuario usuario = usuarioRepository.findUsuarioByEmail(email);

        if(usuario == null){
            throw new UsernameNotFoundException("Usuario no encontrado con email: " + email);
        }

        return UserDetailsImpl.aUserDetailsImpl(usuario);
    }
}
