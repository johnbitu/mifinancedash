package dev.project.finance.configs;

import dev.project.finance.exceptions.UnauthorizedException;
import dev.project.finance.models.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public User getUsuarioAutenticado() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (!(principal instanceof User user)) {
            throw new UnauthorizedException("Usuario nao autenticado");
        }

        return user;
    }

    public Long getUsuarioAutenticadoId() {
        return getUsuarioAutenticado().getId();
    }
}
