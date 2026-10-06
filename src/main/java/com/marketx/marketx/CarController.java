package com.marketx.marketx;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cars")
@CrossOrigin
public class CarController {

    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final CarImageRepository carImageRepository;

    public CarController(
            CarRepository carRepository,
            UserRepository userRepository,
            CarImageRepository carImageRepository) {

        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.carImageRepository = carImageRepository;
    }


    // =========================================================
    // GET ALL CARS
    // Public marketplace
    // =========================================================

    @GetMapping
    public ResponseEntity<?> getAllCars() {

        List<Car> cars = carRepository.findAll();

        return ResponseEntity.ok(cars);
    }


    // =========================================================
    // GET MY CARS
    // Seller can see only their own cars
    // =========================================================

    @GetMapping("/my-cars")
    public ResponseEntity<?> getMyCars(
            Authentication authentication) {

        if (!isAuthenticated(authentication)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Login required.");
        }


        user currentUser =
                getCurrentUser(authentication);


        if (currentUser == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User account not found.");
        }


        if (currentUser.getRole() == null ||
                !currentUser.getRole()
                        .equalsIgnoreCase("SELLER")) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Seller access required.");
        }


        List<Car> cars =
                carRepository.findBySellerId(
                        currentUser.getId()
                );


        return ResponseEntity.ok(cars);
    }


    // =========================================================
    // GET SINGLE CAR
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getCarById(
            @PathVariable Long id) {

        Car car =
                carRepository.findById(id)
                        .orElse(null);


        if (car == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Car not found.");
        }


        return ResponseEntity.ok(car);
    }


    // =========================================================
    // GET SELLER OF A CAR
    // =========================================================

    @GetMapping("/{id}/seller")
    public ResponseEntity<?> getCarSeller(
            @PathVariable Long id) {

        Car car =
                carRepository.findById(id)
                        .orElse(null);


        if (car == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Car not found.");
        }


        Long sellerId =
                car.getSellerId();


        if (sellerId == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Seller information not available.");
        }


        user seller =
                userRepository.findById(sellerId)
                        .orElse(null);


        if (seller == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Seller not found.");
        }


        // Never return password
        seller.setPassword(null);


        return ResponseEntity.ok(seller);
    }


    // =========================================================
    // ADD NEW CAR
    // Seller only
    // =========================================================

    @PostMapping
    public ResponseEntity<?> addCar(
            @RequestBody Car car,
            Authentication authentication) {

        if (!isAuthenticated(authentication)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Login required.");
        }


        if (isAdmin(authentication)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Administrators cannot create seller listings.");
        }


        user currentUser =
                getCurrentUser(authentication);


        if (currentUser == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User account not found.");
        }


        if (currentUser.getRole() == null ||
                !currentUser.getRole()
                        .equalsIgnoreCase("SELLER")) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Seller access required.");
        }


        // Assign the logged-in seller
        car.setSellerId(
                currentUser.getId()
        );


        Car savedCar =
                carRepository.save(car);


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedCar);
    }


    // =========================================================
    // ADD IMAGE TO CAR
    // Owner seller only
    // =========================================================

    @PostMapping("/{carId}/images")
    public ResponseEntity<?> addCarImage(
            @PathVariable Long carId,
            @RequestBody String imageUrl,
            Authentication authentication) {

        if (!isAuthenticated(authentication)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Login required.");
        }


        Car car =
                carRepository.findById(carId)
                        .orElse(null);


        if (car == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Car not found.");
        }


        user currentUser =
                getCurrentUser(authentication);


        if (currentUser == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User account not found.");
        }


        boolean owner =
                car.getSellerId() != null &&
                car.getSellerId()
                        .equals(currentUser.getId());


        if (!owner && !isAdmin(authentication)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("You do not own this car.");
        }


        // Remove surrounding quotes if frontend sends JSON string
        imageUrl =
                imageUrl
                        .replace("\"", "")
                        .trim();


        if (imageUrl.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Image URL cannot be empty.");
        }


        CarImage image =
                new CarImage(
                        imageUrl,
                        car
                );


        CarImage savedImage =
                carImageRepository.save(image);


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedImage);
    }


    // =========================================================
    // UPDATE CAR
    // Owner seller or admin
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCar(
            @PathVariable Long id,
            @RequestBody Car updatedCar,
            Authentication authentication) {

        if (!isAuthenticated(authentication)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Login required.");
        }


        Car existingCar =
                carRepository.findById(id)
                        .orElse(null);


        if (existingCar == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Car not found.");
        }


        user currentUser =
                getCurrentUser(authentication);


        if (currentUser == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User account not found.");
        }


        boolean owner =
                existingCar.getSellerId() != null &&
                existingCar.getSellerId()
                        .equals(currentUser.getId());


        boolean admin =
                isAdmin(authentication);


        if (!owner && !admin) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("You do not have permission to edit this car.");
        }


        // Update fields

        existingCar.setBrand(
                updatedCar.getBrand()
        );

        existingCar.setModel(
                updatedCar.getModel()
        );

        existingCar.setVariant(
                updatedCar.getVariant()
        );

        existingCar.setPrice(
                updatedCar.getPrice()
        );

        existingCar.setFuelType(
                updatedCar.getFuelType()
        );

        existingCar.setTransmission(
                updatedCar.getTransmission()
        );

        existingCar.setManufacturingYear(
                updatedCar.getManufacturingYear()
        );

        existingCar.setKilometersDriven(
                updatedCar.getKilometersDriven()
        );

        existingCar.setLocation(
                updatedCar.getLocation()
        );

        existingCar.setDescription(
                updatedCar.getDescription()
        );

        existingCar.setImage(
                updatedCar.getImage()
        );


        // Never allow seller ownership to be changed
        existingCar.setSellerId(
                existingCar.getSellerId()
        );


        Car savedCar =
                carRepository.save(existingCar);


        return ResponseEntity.ok(savedCar);
    }


    // =========================================================
    // DELETE CAR
    // Owner seller or admin
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCar(
            @PathVariable Long id,
            Authentication authentication) {

        if (!isAuthenticated(authentication)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Login required.");
        }


        Car existingCar =
                carRepository.findById(id)
                        .orElse(null);


        if (existingCar == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Car not found.");
        }


        user currentUser =
                getCurrentUser(authentication);


        if (currentUser == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User account not found.");
        }


        boolean owner =
                existingCar.getSellerId() != null &&
                existingCar.getSellerId()
                        .equals(currentUser.getId());


        boolean admin =
                isAdmin(authentication);


        if (!owner && !admin) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("You do not have permission to delete this car.");
        }


        carRepository.delete(existingCar);


        return ResponseEntity.ok(
                "Car deleted successfully."
        );
    }


    // =========================================================
    // ADMIN DELETE CAR
    // =========================================================

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> adminDeleteCar(
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


    // =========================================================
    // CHECK AUTHENTICATION
    // =========================================================

    private boolean isAuthenticated(
            Authentication authentication) {

        return authentication != null &&
                authentication.isAuthenticated();
    }


    // =========================================================
    // CHECK ADMIN
    // =========================================================

    private boolean isAdmin(
            Authentication authentication) {

        if (!isAuthenticated(authentication)) {
            return false;
        }


        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN"
                                .equals(
                                        authority.getAuthority()
                                )
                );
    }


    // =========================================================
    // GET CURRENT USER
    // =========================================================

    private user getCurrentUser(
            Authentication authentication) {

        if (!isAuthenticated(authentication)) {
            return null;
        }


        String username =
                authentication.getName();


        return userRepository
                .findByEmail(username)
                .orElse(null);
    }

}