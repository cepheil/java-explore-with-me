package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryCreateDto;
import ru.practicum.category.dto.CategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto create(CategoryCreateDto dto);

    CategoryDto update(Long categoryId, CategoryDto dto);

    void delete(Long categoryId);

    List<CategoryDto> findAll(int from, int size);

    CategoryDto findById(Long categoryId);


}
