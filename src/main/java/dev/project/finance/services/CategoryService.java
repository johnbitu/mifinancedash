package dev.project.finance.services;

import dev.project.finance.dtos.CategorySummary;
import dev.project.finance.dtos.CreateCategoryRequest;
import dev.project.finance.exceptions.CategoryEmUsoException;
import dev.project.finance.exceptions.CategoryNotFoundException;
import dev.project.finance.models.Category;
import dev.project.finance.models.User;
import dev.project.finance.repositories.CategoryRepository;
import dev.project.finance.repositories.RecurrenceRepository;
import dev.project.finance.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final RecurrenceRepository recurrenceRepository;

    @Transactional
    public CategorySummary create(CreateCategoryRequest request, User usuarioAutenticado) {
        Category category = Category.builder()
                .nome(request.nome())
                .tipo(request.tipo())
                .user(usuarioAutenticado)
                .build();

        return toSummary(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategorySummary> findAllByUsuario(User usuarioAutenticado) {
        return categoryRepository.findByUserId(usuarioAutenticado.getId())
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategorySummary findByIdAndUsuario(Long categoryId, User usuarioAutenticado) {
        return toSummary(buscarCategoriaPorIdEUsuario(categoryId, usuarioAutenticado.getId()));
    }

    @Transactional
    public CategorySummary update(Long categoryId, CreateCategoryRequest request, User usuarioAutenticado) {
        Category category = buscarCategoriaPorIdEUsuario(categoryId, usuarioAutenticado.getId());

        category.setNome(request.nome());
        category.setTipo(request.tipo());

        return toSummary(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long categoryId, User usuarioAutenticado) {
        Category category = buscarCategoriaPorIdEUsuario(categoryId, usuarioAutenticado.getId());

        if (transactionRepository.existsByCategoryId(categoryId)
                || recurrenceRepository.existsByCategoryIdAndAtivoTrue(categoryId)) {
            throw new CategoryEmUsoException("Nao e possivel excluir categoria vinculada a transacoes ou recorrencias");
        }

        categoryRepository.delete(category);
    }

    private Category buscarCategoriaPorIdEUsuario(Long categoryId, Long userId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Categoria nao encontrada: id=" + categoryId
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
