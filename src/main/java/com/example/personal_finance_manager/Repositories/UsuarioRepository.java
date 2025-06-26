package com.example.personal_finance_manager.Repositories;

import com.example.personal_finance_manager.Models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);

    Usuario findUsuarioByEmail(String email);
}
