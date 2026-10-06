package com.cms.complaints.service;

import com.cms.complaints.dto.CategoryDto;
import com.cms.complaints.dto.PriorityDto;
import com.cms.complaints.dto.UserDto;
import com.cms.complaints.entity.Category;
import com.cms.complaints.entity.Priority;
import com.cms.complaints.entity.Role;
import com.cms.complaints.entity.User;
import com.cms.complaints.exception.BadRequestException;
import com.cms.complaints.exception.ResourceNotFoundException;
import com.cms.complaints.repository.CategoryRepository;
import com.cms.complaints.repository.ComplaintRepository;
import com.cms.complaints.repository.PriorityRepository;
import com.cms.complaints.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PriorityRepository priorityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    public CategoryDto createCategory(String name, String description) {
        if (categoryRepository.findByName(name).isPresent()) {
            throw new BadRequestException("Category with this name already exists");
        }
        Category category = new Category(name, description);
        Category saved = categoryRepository.save(category);
        return mapCategoryToDto(saved);
    }

    public CategoryDto updateCategory(Long id, String name, String description, Boolean active) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (name != null && !name.equals(category.getName()) &&
            categoryRepository.findByName(name).isPresent()) {
            throw new BadRequestException("Category with this name already exists");
        }

        if (name != null) category.setName(name);
        if (description != null) category.setDescription(description);
        if (active != null) category.setActive(active);

        Category updated = categoryRepository.save(category);
        return mapCategoryToDto(updated);
    }

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapCategoryToDto)
                .collect(Collectors.toList());
    }

    public PriorityDto createPriority(String name, Integer level, Integer slaHours) {
        if (priorityRepository.findByName(name).isPresent()) {
            throw new BadRequestException("Priority with this name already exists");
        }
        Priority priority = new Priority(name, level, slaHours);
        Priority saved = priorityRepository.save(priority);
        return mapPriorityToDto(saved);
    }

    public PriorityDto updatePriority(Long id, String name, Integer level, Integer slaHours, Boolean active) {
        Priority priority = priorityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Priority not found"));

        if (name != null && !name.equals(priority.getName()) &&
            priorityRepository.findByName(name).isPresent()) {
            throw new BadRequestException("Priority with this name already exists");
        }

        if (name != null) priority.setName(name);
        if (level != null) priority.setLevel(level);
        if (slaHours != null) priority.setSlaHours(slaHours);
        if (active != null) priority.setActive(active);

        Priority updated = priorityRepository.save(priority);
        return mapPriorityToDto(updated);
    }

    public List<PriorityDto> getAllPriorities() {
        return priorityRepository.findByOrderByLevel().stream()
                .map(this::mapPriorityToDto)
                .collect(Collectors.toList());
    }

    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapUserToDto);
    }

    public UserDto updateUserRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setRole(role);
        User updated = userRepository.save(user);
        return mapUserToDto(updated);
    }

    public UserDto updateUserActive(Long id, Boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(active);
        User updated = userRepository.save(user);
        return mapUserToDto(updated);
    }

    public List<UserDto> getStaffMembers() {
        return userRepository.findByRoleAndActive(Role.STAFF, true).stream()
                .map(this::mapUserToDto)
                .collect(Collectors.toList());
    }

    private CategoryDto mapCategoryToDto(Category category) {
        return new CategoryDto(category.getId(), category.getName(),
                category.getDescription(), category.getActive());
    }

    private PriorityDto mapPriorityToDto(Priority priority) {
        return new PriorityDto(priority.getId(), priority.getName(),
                priority.getLevel(), priority.getSlaHours(), priority.getActive());
    }

    private UserDto mapUserToDto(User user) {
        return new UserDto(user.getId(), user.getFullName(), user.getEmail(),
                user.getPhone(), user.getRole(), user.getActive());
    }
}
