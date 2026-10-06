package com.marketx.marketx;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final SecurityContextRepository securityContextRepository;

    public UserController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository) {

        this.userRepository = userRepository;

        this.passwordEncoder = passwordEncoder;

        this.authenticationManager = authenticationManager;

        this.securityContextRepository =
                securityContextRepository;
    }

    // =========================================================
    // REGISTER
    // =========================================================

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestBody user newUser) {

        // Check email
        if (newUser.getEmail() == null ||
            newUser.getEmail().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Email is required.");
        }

        // Check phone
        if (newUser.getPhone() == null ||
            newUser.getPhone().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Phone number is required.");
        }

        // Validate phone
        if (!newUser.getPhone()
                .trim()
                .matches("\\d{10}")) {

            return ResponseEntity
                    .badRequest()
                    .body(
                        "Phone number must contain exactly 10 digits."
                    );
        }

        // Check password
        if (newUser.getPassword() == null ||
            newUser.getPassword().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Password is required.");
        }

        // Check email already exists
        if (userRepository
                .findByEmail(newUser.getEmail().trim())
                .isPresent()) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email already registered.");
        }

        // Check phone already exists
        if (userRepository
                .findByPhone(newUser.getPhone().trim())
                .isPresent()) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Phone number already registered.");
        }

        // Set email
        newUser.setEmail(
                newUser.getEmail().trim()
        );

        // Set phone
        newUser.setPhone(
                newUser.getPhone().trim()
        );

        // Default role
        if (newUser.getRole() == null ||
            newUser.getRole().trim().isEmpty()) {

            newUser.setRole("BUYER");

        } else {

            String role =
                    newUser.getRole()
                            .trim()
                            .toUpperCase();

            // Normal registration cannot create ADMIN
            if (!role.equals("BUYER") &&
                !role.equals("SELLER")) {

                return ResponseEntity
                        .badRequest()
                        .body(
                            "Role must be BUYER or SELLER."
                        );
            }

            newUser.setRole(role);
        }

        // Encrypt password
        newUser.setPassword(
                passwordEncoder.encode(
                        newUser.getPassword()
                )
        );

        // Save user
        user savedUser =
                userRepository.save(newUser);

        // Never return password
        savedUser.setPassword(null);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedUser);
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
            @RequestBody user loginUser,
            HttpServletRequest request,
            HttpServletResponse response) {

        // -----------------------------------------------------
        // Check email
        // -----------------------------------------------------

        if (loginUser.getEmail() == null ||
            loginUser.getEmail().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Email is required.");
        }

        // -----------------------------------------------------
        // Check password
        // -----------------------------------------------------

        if (loginUser.getPassword() == null ||
            loginUser.getPassword().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Password is required.");
        }

        // -----------------------------------------------------
        // Find user by email
        // -----------------------------------------------------

        user existingUser =
                userRepository
                        .findByEmail(
                                loginUser.getEmail().trim()
                        )
                        .orElse(null);

        if (existingUser == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password.");
        }

        // -----------------------------------------------------
        // Check requested role if supplied
        // -----------------------------------------------------

        if (loginUser.getRole() != null &&
            !loginUser.getRole().trim().isEmpty()) {

            String requestedRole =
                    loginUser.getRole()
                            .trim()
                            .toUpperCase();

            String actualRole =
                    existingUser.getRole();

            if (actualRole == null ||
                !actualRole.equalsIgnoreCase(requestedRole)) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                            "This account is not registered as "
                            + requestedRole + "."
                        );
            }
        }

        // -----------------------------------------------------
        // Authenticate email + password
        // -----------------------------------------------------

        try {

            Authentication authentication =
                    authenticationManager.authenticate(

                            new UsernamePasswordAuthenticationToken(
                                    existingUser.getEmail(),
                                    loginUser.getPassword()
                            )
                    );

            // -------------------------------------------------
            // Create Security Context
            // -------------------------------------------------

            SecurityContext context =
                    SecurityContextHolder
                            .createEmptyContext();

            context.setAuthentication(authentication);

            SecurityContextHolder.setContext(context);

            // -------------------------------------------------
            // Save Security Context to HTTP session
            // -------------------------------------------------

            securityContextRepository.saveContext(
                    context,
                    request,
                    response
            );

            // -------------------------------------------------
            // Never return password
            // -------------------------------------------------

            existingUser.setPassword(null);

            return ResponseEntity.ok(existingUser);

        } catch (AuthenticationException e) {

            SecurityContextHolder.clearContext();

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password.");
        }
    }

    // =========================================================
    // CURRENT USER
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<?> currentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
            !authentication.isAuthenticated() ||
            "anonymousUser".equals(
                    authentication.getPrincipal())) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Not authenticated.");
        }

        user currentUser =
                userRepository
                        .findByEmail(
                                authentication.getName()
                        )
                        .orElse(null);

        if (currentUser == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User not found.");
        }

        // Never return password
        currentUser.setPassword(null);

        return ResponseEntity.ok(currentUser);
    }

    // =========================================================
    // UPDATE PROFILE
    // =========================================================

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestBody user updatedUser,
            Authentication authentication) {

        if (authentication == null ||
            !authentication.isAuthenticated() ||
            "anonymousUser".equals(
                    authentication.getPrincipal())) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Not authenticated.");
        }

        String currentEmail =
                authentication.getName();

        user currentUser =
                userRepository
                        .findByEmail(currentEmail)
                        .orElse(null);

        if (currentUser == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found.");
        }

        // -----------------------------------------------------
        // Update name
        // -----------------------------------------------------

        if (updatedUser.getName() != null &&
            !updatedUser.getName()
                    .trim()
                    .isEmpty()) {

            currentUser.setName(
                    updatedUser.getName().trim()
            );
        }

        // -----------------------------------------------------
        // Update email
        // -----------------------------------------------------

        if (updatedUser.getEmail() != null &&
            !updatedUser.getEmail()
                    .trim()
                    .isEmpty() &&
            !updatedUser.getEmail()
                    .equalsIgnoreCase(
                            currentUser.getEmail()
                    )) {

            if (userRepository
                    .findByEmail(
                            updatedUser.getEmail().trim()
                    )
                    .isPresent()) {

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(
                            "Email already registered."
                        );
            }

            currentUser.setEmail(
                    updatedUser.getEmail().trim()
            );
        }

        // -----------------------------------------------------
        // Update phone
        // -----------------------------------------------------

        if (updatedUser.getPhone() != null &&
            !updatedUser.getPhone()
                    .trim()
                    .isEmpty() &&
            !updatedUser.getPhone()
                    .equals(
                            currentUser.getPhone()
                    )) {

            String newPhone =
                    updatedUser.getPhone().trim();

            if (!newPhone.matches("\\d{10}")) {

                return ResponseEntity
                        .badRequest()
                        .body(
                            "Phone number must contain exactly 10 digits."
                        );
            }

            if (userRepository
                    .findByPhone(newPhone)
                    .isPresent()) {

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(
                            "Phone number already registered."
                        );
            }

            currentUser.setPhone(newPhone);
        }

        user savedUser =
                userRepository.save(
                        currentUser
                );

        // Never return password
        savedUser.setPassword(null);

        return ResponseEntity.ok(savedUser);
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(
            HttpServletRequest request) {

        SecurityContextHolder.clearContext();

        HttpSession session =
                request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity
                .ok(
                    "User logged out successfully."
                );
    }

    // =========================================================
    // GET ALL USERS
    // =========================================================

    @GetMapping
    public List<user> getAllUsers() {

        List<user> users =
                userRepository.findAll();

        for (user currentUser : users) {
            currentUser.setPassword(null);
        }

        return users;
    }

    // =========================================================
    // DELETE USER
    // =========================================================

    @DeleteMapping("/{id}")
    public String deleteUser(
            @PathVariable Long id) {

        if (!userRepository.existsById(id)) {

            return "User not found.";
        }

        userRepository.deleteById(id);

        return "User deleted successfully.";
    }
}