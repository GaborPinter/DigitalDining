package com.DigitalDining.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.DigitalDining.demo.model.DiningTable;
import com.DigitalDining.demo.model.MenuCategory;
import com.DigitalDining.demo.model.Role;
import com.DigitalDining.demo.model.User;
import com.DigitalDining.demo.repository.DiningTableRepository;
import com.DigitalDining.demo.repository.MenuCategoryRepository;
import com.DigitalDining.demo.repository.UserRepository;

@Configuration
public class DataInitializer {

	private final MenuCategoryRepository menuCategoryRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final DiningTableRepository diningTableRepository;

	public DataInitializer(MenuCategoryRepository menuCategoryRepository, UserRepository userRepository,
			PasswordEncoder passwordEncoder, DiningTableRepository diningTableRepository) {
		this.menuCategoryRepository = menuCategoryRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.diningTableRepository = diningTableRepository;
	}

	@Bean
    CommandLineRunner initCategories() {
        return args -> {
            saveIfMissing("Leves", "🥣", 1);
            saveIfMissing("Főétel", "🍝", 2);
            saveIfMissing("Desszert", "🍰", 3);
            saveIfMissing("Ital", "🥤", 4);
        };
    }
	
	@Bean
    CommandLineRunner initUsers() {
        return args -> {
            saveUserIfMissing("Admin", "admin@gmail.com", "Admin", Role.ADMIN);
            saveUserIfMissing("User", "user@gmail.com", "User", Role.USER);
        };
    }

    private void saveIfMissing(String name, String icon, int order) {
        if (!menuCategoryRepository.existsByNameIgnoreCase(name)) {
            menuCategoryRepository.save(new MenuCategory(name, icon, order, true));
        }
    }
    
    private void saveUserIfMissing(String username, String email, String rawPassword, Role role) {
        boolean usernameExists = userRepository.existsByUsername(username);
        boolean emailExists = userRepository.existsByEmail(email);

        if (usernameExists || emailExists) {
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);

        userRepository.save(user);
    }
    
    @Bean
    CommandLineRunner initDiningTables() {
        return args -> {
            saveTableIfMissing("T1", 2);
            saveTableIfMissing("T2", 2);
            saveTableIfMissing("T3", 2);

            saveTableIfMissing("T4", 4);
            saveTableIfMissing("T5", 4);
            saveTableIfMissing("T6", 4);

            saveTableIfMissing("T7", 6);
            saveTableIfMissing("T8", 6);

            saveTableIfMissing("T9", 8);
        };
    }

    private void saveTableIfMissing(String tableCode, int capacity) {
        if (!diningTableRepository.existsByTableCodeIgnoreCase(tableCode)) {
            diningTableRepository.save(new DiningTable(tableCode, capacity, true));
        }
    }
}
