package dev.project.finance.services;

import dev.project.finance.dtos.UserSummary;
import dev.project.finance.models.Roles;
import dev.project.finance.models.User;
import dev.project.finance.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserSummary> findAll(){
        return userRepository.findAll()
                .stream()
                .map(this::toSummary)
                .toList();
    }

    public UserSummary findById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return toSummary(user);
    }

    public UserSummary toSummary(User user){
        return new UserSummary(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getRole()
        );
    }
}
