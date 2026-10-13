package com.example.MilkSystemApplication.config;

import com.example.MilkSystemApplication.model.AdminUser;
import com.example.MilkSystemApplication.model.Farmer;
import com.example.MilkSystemApplication.repository.AdminUserRepository;
import com.example.MilkSystemApplication.repository.FarmerRepository;
import com.example.MilkSystemApplication.security.JwtAuthenticationFilter;
import com.example.MilkSystemApplication.security.SubscriptionEnforcementFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private AdminUserRepository adminUserRepo;

    @Autowired
    private FarmerRepository farmerRepo;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private SubscriptionEnforcementFilter subscriptionEnforcementFilter;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://localhost:8080,https://app.milkmanagementsystem.com}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        return username -> {
            String clean = username != null ? username.trim() : "";
            
            // 1. Check SQL Admin User (by username, email or phone)
            Optional<AdminUser> adminOpt = adminUserRepo.findByUsername(clean.toLowerCase());
            if (adminOpt.isEmpty()) {
                adminOpt = adminUserRepo.findByEmail(clean.toLowerCase());
            }
            if (adminOpt.isEmpty()) {
                adminOpt = adminUserRepo.findByPhone(clean);
            }

            if (adminOpt.isPresent()) {
                AdminUser admin = adminOpt.get();
                String pass = admin.getPassword();
                if (!pass.startsWith("$2a$") && !pass.startsWith("$2b$") && !pass.startsWith("$2y$")) {
                    pass = passwordEncoder.encode(pass);
                }
                return new User(
                        admin.getUsername(),
                        pass,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );
            }

            // Fallback for bootstrap admin if table empty
            if ("admin".equalsIgnoreCase(clean)) {
                return User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .roles("ADMIN")
                        .build();
            }

            // 2. Check SQL Farmer (by Farmer ID, Unique Code, or Phone)
            Optional<Farmer> farmerOpt = Optional.empty();
            try {
                Long farmerId = Long.parseLong(clean);
                farmerOpt = farmerRepo.findById(farmerId);
            } catch (NumberFormatException ignored) {}

            if (farmerOpt.isEmpty()) {
                farmerOpt = farmerRepo.findByCodeIgnoreCase(clean);
            }

            if (farmerOpt.isEmpty()) {
                farmerOpt = farmerRepo.findByPhone(clean);
            }

            if (farmerOpt.isPresent()) {
                Farmer farmer = farmerOpt.get();
                String pass = farmer.getPassword();
                if (pass == null || pass.trim().isEmpty()) {
                    pass = passwordEncoder.encode("farmer123");
                } else if (!pass.startsWith("$2a$") && !pass.startsWith("$2b$") && !pass.startsWith("$2y$")) {
                    pass = passwordEncoder.encode(pass);
                }

                return new org.springframework.security.core.userdetails.User(
                        String.valueOf(farmer.getId()),
                        pass,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_FARMER"))
                );
            }

            throw new UsernameNotFoundException("User not found with identifier: " + username);
        };
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isFarmer = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_FARMER"));

            if (isAdmin) {
                response.sendRedirect("/admin/dashboard");
            } else if (isFarmer) {
                response.sendRedirect("/farmer/dashboard");
            } else {
                response.sendRedirect("/");
            }
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Tenant-ID", "Accept", "Origin", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/signup", "/api/**", "/h2-console/**", "/collections/**", "/farmers/**")
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        // Public Endpoints & Static Assets
                        .requestMatchers("/", "/signup", "/login", "/home", "/pricing", "/contact", "/about").permitAll()
                        .requestMatchers("/style.css", "/app.js", "/css/**", "/js/**", "/images/**", "/webjars/**", "/h2-console/**").permitAll()
                        .requestMatchers("/assets/**", "/*.png", "/*.ico", "/*.json", "/*.svg").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        // Public API routes
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/subscriptions/plans", "/api/subscriptions/webhook").permitAll()
                        .requestMatchers("/api/storage/**").permitAll()
                        // Legacy & Open collection endpoints
                        .requestMatchers("/farmers/**", "/collections/**").permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/farmer/**").hasRole("FARMER")
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(customAuthenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(subscriptionEnforcementFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}