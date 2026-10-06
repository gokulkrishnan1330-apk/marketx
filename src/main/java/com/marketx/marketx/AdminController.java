package com.marketx.marketx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    private final UserRepository userRepository;
    private final CarRepository carRepository;

    public AdminController(UserRepository userRepository,
                           CarRepository carRepository) {
        this.userRepository = userRepository;
        this.carRepository = carRepository;
    }


    // =========================
    // ADMIN CHECK
    // =========================

    private boolean isAdmin(Authentication authentication) {

        if (authentication == null) {
            return false;
        }

        if (!authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(authority.getAuthority()));
    }


    // =========================
    // ADMIN STATS
    // =========================

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(Authentication authentication) {

        if (!isAdmin(authentication)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Admin access required.");
        }

        List<user> users = userRepository.findAll();

        long totalUsers = users.size();

        long sellers = users.stream()
                .filter(u ->
                        u.getRole() != null &&
                        u.getRole().equalsIgnoreCase("SELLER"))
                .count();

        long buyers = users.stream()
                .filter(u ->
                        u.getRole() != null &&
                        u.getRole().equalsIgnoreCase("BUYER"))
                .count();

        long administrators = users.stream()
                .filter(u ->
                        u.getRole() != null &&
                        u.getRole().equalsIgnoreCase("ADMIN"))
                .count();

        long totalCars = carRepository.count();

        Map<String, Long> stats = new HashMap<>();

        stats.put("totalUsers", totalUsers);
        stats.put("sellers", sellers);
        stats.put("buyers", buyers);
        stats.put("administrators", administrators);
        stats.put("totalCars", totalCars);

        return ResponseEntity.ok(stats);
    }


    // =========================
    // GET ALL USERS
    // =========================

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(Authentication authentication) {

        if (!isAdmin(authentication)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Admin access required.");
        }

        List<user> users = userRepository.findAll();

        // Never send passwords to frontend
        for (user currentUser : users) {
            currentUser.setPassword(null);
        }

        return ResponseEntity.ok(users);
    }


    // =========================
    // GET ALL CARS
    // =========================

    @GetMapping("/cars")
    public ResponseEntity<?> getAllCars(Authentication authentication) {

        if (!isAdmin(authentication)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Admin access required.");
        }

        return ResponseEntity.ok(carRepository.findAll());
    }


    // =========================
    // DELETE USER
    // =========================

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id,
            Authentication authentication) {

        if (!isAdmin(authentication)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Admin access required.");
        }

        user existingUser =
                userRepository.findById(id).orElse(null);

        if (existingUser == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found.");
        }


        // Prevent deleting an administrator
        if (existingUser.getRole() != null &&
                existingUser.getRole().equalsIgnoreCase("ADMIN")) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Administrator accounts cannot be deleted.");
        }


        userRepository.deleteById(id);

        return ResponseEntity.ok(
                "User deleted successfully."
        );
    }


    // =========================
    // DELETE CAR
    // =========================

    @DeleteMapping("/cars/{id}")
    public ResponseEntity<String> deleteCar(
            @PathVariable Long id,
            Authentication authentication) {

        if (!isAdmin(authentication)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Admin access required.");
        }

        if (!carRepository.existsById(id)) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Car not found.");
        }

        carRepository.deleteById(id);

        return ResponseEntity.ok(
                "Car deleted successfully by admin."
        );
    }
}