package com.bonnarotec.taskmanager.security.oauth2;

import com.bonnarotec.taskmanager.domain.user.AuthProvider;
import com.bonnarotec.taskmanager.domain.user.User;
import com.bonnarotec.taskmanager.domain.user.UserRole;
import com.bonnarotec.taskmanager.repository.UserRepository;
import com.bonnarotec.taskmanager.security.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public OAuth2AuthenticationSuccessHandler(JwtUtil jwtUtil, UserRepository userRepository,
                                              PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();

        String provider = oauthToken.getAuthorizedClientRegistrationId();

        // Debug logging
        System.out.println("OAuth2 Provider: " + provider);
        System.out.println("OAuth2 Attributes: " + oAuth2User.getAttributes());

        String email = extractEmail(oAuth2User.getAttributes(), provider);
        String name = extractName(oAuth2User.getAttributes(), provider);
        String providerId = oAuth2User.getName();

        // More debug logging
        System.out.println("Extracted Email: " + email);
        System.out.println("Extracted Name: " + name);
        System.out.println("Provider ID: " + providerId);

        User user = processOAuthUser(provider, email, name, providerId);

        String token = jwtUtil.generateToken(user);
        String redirectUrl = UriComponentsBuilder.fromUriString("/oauth2/redirect")
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private User processOAuthUser(String provider, String email, String name, String providerId) {
        AuthProvider authProvider = AuthProvider.valueOf(provider.toUpperCase());

        System.out.println("Vai procurar user");
        Optional<User> userOptional = userRepository.findByEmail(email);
        System.out.println("procurou");

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            System.out.println("Found existing user: " + existingUser.getUsername() +
                    " with provider: " + existingUser.getProvider());

            // Update the user with new information from OAuth provider
            existingUser.setUsername(name);

            // Update provider details if this user is using the same provider
            if (existingUser.getProvider() == authProvider) {
                existingUser.setProviderId(providerId);
            }

            existingUser.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(existingUser);
        } else {
            // Create new user
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(name);
            newUser.setProvider(authProvider);
            newUser.setProviderId(providerId);
            newUser.setPassword(passwordEncoder.encode(providerId));
            newUser.setRoles(Set.of(UserRole.USER));

            // Make sure to set the date fields
            LocalDateTime now = LocalDateTime.now();
            newUser.setCreatedAt(now);
            newUser.setUpdatedAt(now);

            // Set security-related fields to true
            newUser.setEnabled(true);
            newUser.setAccountNonExpired(true);
            newUser.setAccountNonLocked(true);
            newUser.setCredentialsNonExpired(true);

            System.out.println("Creating new user with email: " + email);
            return userRepository.save(newUser);
        }
    }

    private String extractEmail(Map<String, Object> attributes, String provider) {
        if ("google".equals(provider)) {
            return (String) attributes.get("email");
        } else if ("github".equals(provider)) {
            // GitHub might not include email directly in attributes if it's private
            String email = (String) attributes.get("email");

            if (email == null || email.isEmpty()) {
                // Try to get from the emails array if available
                Object emailsObj = attributes.get("emails");
                if (emailsObj instanceof List) {
                    List<?> emails = (List<?>) emailsObj;
                    if (!emails.isEmpty() && emails.get(0) instanceof Map) {
                        Map<?, ?> emailData = (Map<?, ?>) emails.get(0);
                        email = (String) emailData.get("email");
                    }
                }

                // If still null, we might need a fallback
                if (email == null || email.isEmpty()) {
                    // Use login + placeholder as fallback
                    String login = (String) attributes.get("login");
                    if (login != null) {
                        email = login + "@github.user";
                    } else {
                        // Ultimate fallback to prevent null email
                        email = "unknown+" + System.currentTimeMillis() + "@github.user";
                    }
                }
            }
            return email;
        }
        return "";
    }

    private String extractName(Map<String, Object> attributes, String provider) {
        if ("google".equals(provider)) {
            return (String) attributes.get("name");
        } else if ("github".equals(provider)) {
            String name = (String) attributes.get("name");
            if (name == null || name.isEmpty()) {
                name = (String) attributes.get("login");
            }
            return name;
        }
        return "";
    }
}
