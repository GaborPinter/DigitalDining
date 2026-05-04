package com.DigitalDining.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.DigitalDining.demo.dto.MenuItemForm;
import com.DigitalDining.demo.model.MenuItem;
import com.DigitalDining.demo.service.MenuItemService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/menu")
public class MenuController {

	private final MenuItemService menuItemService;

	@Autowired
    public MenuController(MenuItemService menuItemService) {
		this.menuItemService = menuItemService;
	}

	@GetMapping
	public String menuPage(@RequestParam(value = "search", required = false) String search, Model model) {
	    model.addAttribute("sections", menuItemService.getMenuSections(search));
	    model.addAttribute("search", search);
	    return "menu/list";
	}

	@GetMapping("/add")
	public String showAddForm(Model model) {
	    model.addAttribute("itemForm", new MenuItemForm());
	    model.addAttribute("categories", menuItemService.getActiveCategories());
	    model.addAttribute("isEdit", false);
	    model.addAttribute("currentImagePath", null);
	    return "menu/form";
	}

	@PostMapping("/add")
	public String createMenuItem(
	        @Valid @ModelAttribute("itemForm") MenuItemForm form,
	        BindingResult bindingResult,
	        @RequestParam(value = "image", required = false) MultipartFile image,
	        Model model
	) throws Exception {

	    if (bindingResult.hasErrors()) {
	        model.addAttribute("categories", menuItemService.getActiveCategories());
	        model.addAttribute("isEdit", false);
	        model.addAttribute("currentImagePath", null);
	        return "menu/form";
	    }

	    menuItemService.createMenuItem(form, image);
	    return "redirect:/menu?success";
	}
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        MenuItem item = menuItemService.getMenuItemById(id);

        MenuItemForm form = new MenuItemForm();
        form.setName(item.getName());
        form.setDescription(item.getDescription());
        form.setIngredients(item.getIngredients());
        form.setAllergens(item.getAllergens());
        form.setPrice(item.getPrice());
        form.setCalories(item.getCalories());
        form.setPrepTimeMinutes(item.getPrepTimeMinutes());
        form.setVegetarian(item.isVegetarian());
        form.setVegan(item.isVegan());
        form.setSpicy(item.isSpicy());
        form.setAvailable(item.isAvailable());
        form.setCategoryId(item.getCategory().getId());

        model.addAttribute("itemForm", form);
        model.addAttribute("categories", menuItemService.getActiveCategories());
        model.addAttribute("isEdit", true);
        model.addAttribute("itemId", id);
        model.addAttribute("currentImagePath", item.getImagePath());

        return "menu/form";
    }

    @PostMapping("/edit/{id}")
    public String updateMenuItem(
            @PathVariable Long id,
            @Valid @ModelAttribute("itemForm") MenuItemForm form,
            BindingResult bindingResult,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Model model
    ) throws Exception {

        if (bindingResult.hasErrors()) {
            MenuItem item = menuItemService.getMenuItemById(id);
            model.addAttribute("categories", menuItemService.getActiveCategories());
            model.addAttribute("isEdit", true);
            model.addAttribute("itemId", id);
            model.addAttribute("currentImagePath", item.getImagePath());
            return "menu/form";
        }

        menuItemService.updateMenuItem(id, form, image);
        return "redirect:/menu?updated";
    }
    
    @PostMapping("/delete/{id}")
    public String deleteMenuItem(@PathVariable Long id) {
        menuItemService.deleteMenuItem(id);
        return "redirect:/menu?deleted";
    }
}
