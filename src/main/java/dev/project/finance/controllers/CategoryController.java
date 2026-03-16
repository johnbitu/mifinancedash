package dev.project.finance.controllers;

import dev.project.finance.dtos.CategorySummary;
import dev.project.finance.dtos.CreateCategoryRequest;
import dev.project.finance.models.User;
import dev.project.finance.services.CategoryService;
import dev.project.finance.configs.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CategoryController {

    private final CategoryService categoryService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<CategorySummary> criar(
            @RequestBody @Valid CreateCategoryRequest request
    ) {
        User usuario = securityUtils.getUsuarioAutenticado();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.create(request, usuario));
    }

    @GetMapping
    public ResponseEntity<List<CategorySummary>> listar() {
        User usuario = securityUtils.getUsuarioAutenticado();
        return ResponseEntity.ok(categoryService.findAllByUsuario(usuario));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategorySummary> buscarPorId(@PathVariable Long id) {
        User usuario = securityUtils.getUsuarioAutenticado();
        return ResponseEntity.ok(categoryService.findByIdAndUsuario(id, usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategorySummary> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid CreateCategoryRequest request
    ) {
        User usuario = securityUtils.getUsuarioAutenticado();
        return ResponseEntity.ok(categoryService.update(id, request, usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        User usuario = securityUtils.getUsuarioAutenticado();
        categoryService.delete(id, usuario);
        return ResponseEntity.noContent().build();
    }
}