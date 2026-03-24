package dev.project.finance.services;

import dev.project.finance.dtos.RegisterRequest;
import dev.project.finance.exceptions.EmailAlreadyInUseException;
import dev.project.finance.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterService registerService;

    @Test
    void lancaConflitoQuandoEmailJaExiste() {
        RegisterRequest request = new RegisterRequest("Teste", "dup@example.com", "Senha12345");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(EmailAlreadyInUseException.class, () -> registerService.register(request));
    }
}
