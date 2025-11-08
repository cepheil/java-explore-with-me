package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryCreateDto;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;


    @Override
    @Transactional
    public CategoryDto create(CategoryCreateDto dto) {
        Category category = CategoryMapper.toEntity(dto);
        String name = category.getName();

        if (name.length() < 1 || name.length() > 50) {
            log.warn("Invalid category name length: ({} chars)", name.length());
            throw new ValidationException("category name length must be between 1 and 50 characters");
        }


        if (categoryRepository.existsByName(category.getName())) {
            log.warn("Category already exists: {}", category.getName());
            throw new ConflictException("Category already exists: " + category.getName());
        }

        Category saved = categoryRepository.save(category);
        log.info("New category created: ID={} name={}", saved.getId(), saved.getName());

        return CategoryMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CategoryDto update(Long categoryId, CategoryDto dto) {
        if (categoryId == null) {
            log.warn("categoryId must not be null");
            throw new BadRequestException("categoryId must not be null");
        }

        Category existing = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found: id=" + categoryId));

        String newName = dto.getName().trim();

        if (newName.length() < 1 || newName.length() > 50) {
            log.warn("Invalid category name length: ({} chars)", newName.length());
            throw new ValidationException("category name length must be between 1 and 50 characters");
        }

        if (!existing.getName().equals(newName) && categoryRepository.existsByName(newName)) {
            log.warn("Category name conflict on update: {} (id={})", newName, categoryId);
            throw new ConflictException("Category already exists: " + newName);
        }

        existing.setName(newName);
        Category saved = categoryRepository.save(existing);
        log.info("Category updated: id={} name={}", saved.getId(), saved.getName());
        return CategoryMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long categoryId) {
        if (categoryId == null) {
            log.warn("categoryId must not be null");
            throw new BadRequestException("categoryId must not be null");
        }

        if (!categoryRepository.existsById(categoryId)) {
            log.warn("Category with id={} was not found", categoryId);
            throw new NotFoundException("Category with id=" + categoryId + " was not found");
        }

        if (eventRepository.existsByCategoryId(categoryId)) {
            log.warn("Category is not empty");
            throw new ConflictException("The category is not empty");
        }

        categoryRepository.deleteById(categoryId);
        log.info("Category deleted: id={}", categoryId);

    }

    @Override
    public List<CategoryDto> findAll(int from, int size) {
        if (from < 0 || size <= 0) {
            log.error("Invalid pagination params: from={}, size={}", from, size);
            throw new BadRequestException("Invalid pagination params: from >= 0 and size > 0 are required");
        }

        PageRequest page = PageRequest.of(from / size, size, Sort.by("id").ascending());

        log.debug("Fetching categories page: from={}, size={}", from, size);

        return categoryRepository.findAll(page)
                .map(CategoryMapper::toDto)
                .getContent();
    }


    @Override
    public CategoryDto findById(Long categoryId) {
        if (categoryId == null) {
            log.warn("categoryId must not be null");
            throw new BadRequestException("categoryId must not be null");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found: id=" + categoryId));

        return CategoryMapper.toDto(category);

    }
}
