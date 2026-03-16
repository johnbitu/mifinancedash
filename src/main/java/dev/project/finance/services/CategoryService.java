package dev.project.finance.services;

import dev.project.finance.dtos.CategorySummary;
import dev.project.finance.dtos.CreateCategoryRequest;
import dev.project.finance.exceptions.CategoryNotFoundException;
import dev.project.finance.models.Category;
import dev.project.finance.models.User;
import dev.project.finance.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategorySummary create(CreateCategoryRequest request, User usuarioAutenticado) {
        Category category = Category.builder()
                .nome(request.nome())
                .tipo(request.tipo())
                .criadoEm(LocalDateTime.now())
                .user(usuarioAutenticado)
                .build();

        return toSummary(categoryRepository.save(category));
    }

    public List<CategorySummary> findAllByUsuario(User usuarioAutenticado) {
        return categoryRepository.findByUserId(usuarioAutenticado.getId())
                .stream()
                .map(this::toSummary)
                .toList();
    }

    public CategorySummary findByIdAndUsuario(Long categoryId, User usuarioAutenticado) {
        return toSummary(buscarCategoriaPorIdEUsuario(categoryId, usuarioAutenticado.getId()));
    }

    public CategorySummary update(Long categoryId, CreateCategoryRequest request, User usuarioAutenticado) {
        Category category = buscarCategoriaPorIdEUsuario(categoryId, usuarioAutenticado.getId());

        category.setNome(request.nome());
        category.setTipo(request.tipo());

        return toSummary(categoryRepository.save(category));
    }

    public void delete(Long categoryId, User usuarioAutenticado) {
        Category category = buscarCategoriaPorIdEUsuario(categoryId, usuarioAutenticado.getId());
        categoryRepository.delete(category);
    }

    // — Métodos privados de suporte —

    private Category buscarCategoriaPorIdEUsuario(Long categoryId, Long userId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Categoria não encontrada: id=" + categoryId
                ));
    }

    public CategorySummary toSummary(Category category) {
        return new CategorySummary(
                category.getId(),
                category.getNome(),
                category.getTipo(),
                category.getCriadoEm()
        );
    }
}