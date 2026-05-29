package com.proyecto.version1.config;

import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final EmpleadosRepository empleadosRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        return empleadosRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
