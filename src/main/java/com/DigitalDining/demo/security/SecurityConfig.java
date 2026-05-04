package com.DigitalDining.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.DigitalDining.demo.service.DatabaseUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final DatabaseUserDetailsService userDetailsService;

    public SecurityConfig(DatabaseUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder =
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth.requestMatchers(
				"/", "/mainPage", "/about", "/contact", "/contact/send",
                "/menu", "/menu/add", "/menu/edit/{id}", "/menu/delete/{id}",
                "/food-drinks/**", "/faq",
                "/auth/login", "/auth/register", "/auth/forgot-password", "/auth/reset-password", "/cart", 
                "/css/**", "/js/**", "/images/**", "/pdfs/**"
				).permitAll().anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .failureUrl("/auth/login?error")
                .permitAll()
            )
            .logout(logout -> logout
            	.logoutUrl("/auth/do-logout")
                .logoutSuccessUrl("/auth/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
