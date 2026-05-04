package com.DigitalDining.demo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.DigitalDining.demo.dto.MenuItemForm;
import com.DigitalDining.demo.model.MenuCategory;
import com.DigitalDining.demo.model.MenuCategorySection;
import com.DigitalDining.demo.model.MenuItem;
import com.DigitalDining.demo.repository.MenuCategoryRepository;
import com.DigitalDining.demo.repository.MenuItemRepository;

@Service
public class MenuItemService {

	private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    
    @Autowired
    public MenuItemService(MenuItemRepository menuItemRepository, MenuCategoryRepository menuCategoryRepository) {
		this.menuItemRepository = menuItemRepository;
		this.menuCategoryRepository = menuCategoryRepository;
	}

	@Value("${app.upload-dir}")
    private String uploadDir;

    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAllByOrderByCategory_DisplayOrderAscNameAsc();
    }

    public List<MenuCategory> getActiveCategories() {
        return menuCategoryRepository.findAllByActiveTrueOrderByDisplayOrderAscNameAsc();
    }
    
    public List<MenuCategorySection> getMenuSections(String search) {
        List<MenuCategory> categories = getActiveCategories();
        List<MenuItem> items = searchMenuItems(search);

        Map<Long, List<MenuItem>> itemsByCategoryId = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getCategory().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return categories.stream()
                .map(category -> new MenuCategorySection(
                        category,
                        pluralizeCategoryName(category.getName()),
                        itemsByCategoryId.getOrDefault(category.getId(), List.of())
                ))
                .filter(section -> !section.getItems().isEmpty())
                .toList();
    }

    private String pluralizeCategoryName(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }

        String normalized = name.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "leves" -> "Levesek";
            case "főétel" -> "Főételek";
            case "desszert" -> "Desszertek";
            case "ital" -> "Italok";
            default -> Character.toUpperCase(name.trim().charAt(0)) + name.trim().substring(1) + "ek";
        };
    }
    
    @Transactional(readOnly = true)
    public MenuItem getMenuItemById(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("A menüelem nem található"));
    }

    @Transactional
    public MenuItem createMenuItem(MenuItemForm form, MultipartFile image) throws IOException {
        MenuCategory category = menuCategoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("A kategória nem található"));

        MenuItem item = new MenuItem();
        item.setName(form.getName());
        item.setDescription(form.getDescription());
        item.setIngredients(form.getIngredients());
        item.setAllergens(form.getAllergens());
        item.setPrice(form.getPrice());
        item.setCalories(form.getCalories());
        item.setPrepTimeMinutes(form.getPrepTimeMinutes());
        item.setVegetarian(form.isVegetarian());
        item.setVegan(form.isVegan());
        item.setSpicy(form.isSpicy());
        item.setAvailable(form.isAvailable());
        item.setCategory(category);

        if (image != null && !image.isEmpty()) {
            String originalFilename = StringUtils.cleanPath(image.getOriginalFilename() != null ? image.getOriginalFilename() : "food");
            String extension = StringUtils.getFilenameExtension(originalFilename);
            String fileName = UUID.randomUUID() + (extension != null ? "." + extension : "");

            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(image.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            item.setImagePath("/food-drinks/" + fileName);
        }

        return menuItemRepository.save(item);
    }
    
    @Transactional
    public MenuItem updateMenuItem(Long id, MenuItemForm form, MultipartFile image) throws IOException {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("A menüelem nem található"));

        MenuCategory category = menuCategoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("A kategória nem található"));

        // Szöveges / numerikus / logikai mezők frissítése
        applyFormToItem(item, form, category);

        // Ha új kép érkezett, töröljük a régit és mentsük az újat
        if (image != null && !image.isEmpty()) {
            deleteImageIfExists(item.getImagePath());

            String newImagePath = saveImage(image);
            item.setImagePath(newImagePath);
        }

        return menuItemRepository.save(item);
    }
    
    public List<MenuItem> searchMenuItems(String search) {
        if (search == null || search.trim().isEmpty()) {
            return menuItemRepository.findAllByOrderByCategory_DisplayOrderAscNameAsc();
        }
        return menuItemRepository.findByNameContainingIgnoreCaseOrderByCategory_DisplayOrderAscNameAsc(search.trim());
    }
    
    @Transactional
    public void deleteMenuItem(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("A menüelem nem található"));

        // 1. Kép törlése a helyi mappából / volume-ból
        if (item.getImagePath() != null && !item.getImagePath().isBlank()) {
            try {
                String fileName = Paths.get(item.getImagePath()).getFileName().toString();
                Path imagePath = Paths.get(uploadDir).resolve(fileName).normalize();
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                throw new RuntimeException("A kép törlése sikertelen: " + item.getImagePath(), e);
            }
        }

        // 2. Rekord törlése adatbázisból
        menuItemRepository.delete(item);
    }
    
    private void applyFormToItem(MenuItem item, MenuItemForm form, MenuCategory category) {
        item.setName(form.getName());
        item.setDescription(form.getDescription());
        item.setIngredients(form.getIngredients());
        item.setAllergens(form.getAllergens());
        item.setPrice(form.getPrice());
        item.setCalories(form.getCalories());
        item.setPrepTimeMinutes(form.getPrepTimeMinutes());
        item.setVegetarian(form.isVegetarian());
        item.setVegan(form.isVegan());
        item.setSpicy(form.isSpicy());
        item.setAvailable(form.isAvailable());
        item.setCategory(category);
    }

    private String saveImage(MultipartFile image) throws IOException {
        String originalFilename = StringUtils.cleanPath(
                image.getOriginalFilename() != null ? image.getOriginalFilename() : "food"
        );
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String fileName = UUID.randomUUID() + (extension != null ? "." + extension : "");

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        Path targetLocation = uploadPath.resolve(fileName);
        Files.copy(image.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return "/food-drinks/" + fileName;
    }

    private void deleteImageIfExists(String imagePath) {
        if (imagePath != null && !imagePath.isBlank()) {
            try {
                String fileName = Paths.get(imagePath).getFileName().toString();
                Path fullPath = Paths.get(uploadDir).resolve(fileName).normalize();
                Files.deleteIfExists(fullPath);
            } catch (IOException e) {
                throw new RuntimeException("A kép törlése sikertelen: " + imagePath, e);
            }
        }
    }
}
