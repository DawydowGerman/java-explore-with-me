package ru.practicum.category.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.storage.CategoryJPARepository;
import ru.practicum.event.storage.EventJPARepository;
import ru.practicum.exception.ConstraintException;
import ru.practicum.exception.NotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryJPARepository categoryJPARepository;
    private final EventJPARepository eventJPARepository;

    @Autowired
    public CategoryServiceImpl(CategoryJPARepository categoryJPARepository,
                               EventJPARepository eventJPARepository) {
        this.categoryJPARepository = categoryJPARepository;
        this.eventJPARepository = eventJPARepository;
    }

    @Transactional
    public CategoryDto saveCategory(NewCategoryDto newCategoryDto) {
        Category category = CategoryMapper.toModelFromNew(newCategoryDto);

        if (categoryJPARepository.existsByName(newCategoryDto.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "could not execute statement; SQL [n/a]; constraint [" + newCategoryDto.getName() + "]; nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement");
        }

        category = categoryJPARepository.save(category);
        return CategoryMapper.toDto(category);
    }

    public List<CategoryDto> getCategories(Integer from, Integer size) {
        if (categoryJPARepository.findAllByIdWithPagination(from, size).isEmpty()) {
            return Collections.emptyList();
        }
        return categoryJPARepository.findAllByIdWithPagination(from, size).get().stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
     }

    public CategoryDto getCategoryById(Long catId) {
        if (categoryJPARepository.getCategoryById(catId).isEmpty()) {
            throw new NotFoundException("Category with id=" + catId + " was not found");
        }
        return CategoryMapper.toDto(categoryJPARepository.getCategoryById(catId).get());
    }

    @Transactional
    public CategoryDto update(Long catId, NewCategoryDto newCategoryDto) {
        if (!categoryJPARepository.existsById(catId)) {
         throw new NotFoundException("Category with id=" + catId + " was not found");
        }
        Category category = categoryJPARepository.findById(catId).get();
        Category incomingCategoryByName = categoryJPARepository.findByName(newCategoryDto.getName());
        if (categoryJPARepository.existsByName(newCategoryDto.getName()) && !incomingCategoryByName.getId().equals(catId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "could not execute statement; SQL [n/a]; constraint [" + newCategoryDto.getName() + "]; nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement");
        }
        if (newCategoryDto.getName() != null && !newCategoryDto.getName().isBlank()) {
            category.setName(newCategoryDto.getName());
        }
        category = categoryJPARepository.save(category);
        return CategoryMapper.toDto(category);
    }

    @Transactional
    public void remove(Long id) {
        if (eventJPARepository.existsByCategory(id)) {
            throw new ConstraintException("Cannot delete a category linked to an event");
        }
        categoryJPARepository.deleteById(id);
    }
}