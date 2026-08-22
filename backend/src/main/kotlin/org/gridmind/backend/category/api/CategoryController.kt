package org.gridmind.backend.category.api

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.gridmind.backend.category.application.CategoryService
import org.gridmind.backend.category.domain.Category
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val categoryService: CategoryService,
) {
    @GetMapping
    fun listCategories(): List<CategoryResponse> =
        categoryService.findAll().map(CategoryResponse::from)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCategory(@Valid @RequestBody request: CreateCategoryRequest): CategoryResponse =
        CategoryResponse.from(categoryService.create(request.name))
}

data class CreateCategoryRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,
)

data class CategoryResponse(
    val id: Long?,
    val name: String,
) {
    companion object {
        fun from(category: Category): CategoryResponse = CategoryResponse(
            id = category.id,
            name = category.name,
        )
    }
}
