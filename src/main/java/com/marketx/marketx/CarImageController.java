package com.marketx.marketx;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/car-images")
public class CarImageController {

    private final CarImageRepository carImageRepository;
    private final CarRepository carRepository;

    public CarImageController(
            CarImageRepository carImageRepository,
            CarRepository carRepository) {

        this.carImageRepository = carImageRepository;
        this.carRepository = carRepository;
    }

    // =====================================================
    // GET ALL IMAGES FOR A CAR
    // =====================================================

    @GetMapping("/car/{carId}")
    public ResponseEntity<?> getImages(
            @PathVariable Long carId) {

        if (!carRepository.existsById(carId)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Car not found.");
        }

        List<CarImage> images =
                carImageRepository.findByCarId(carId);

        return ResponseEntity.ok(images);
    }

    // =====================================================
    // ADD IMAGE URL TO A CAR
    // =====================================================

    @PostMapping("/car/{carId}")
    public ResponseEntity<?> addImage(
            @PathVariable Long carId,
            @RequestBody CarImage carImage,
            Authentication authentication) {

        // ---------------------------------------------
        // CHECK LOGIN
        // ---------------------------------------------

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Please login first.");
        }

        // ---------------------------------------------
        // CHECK CAR
        // ---------------------------------------------

        Car car =
                carRepository.findById(carId)
                        .orElse(null);

        if (car == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Car not found.");
        }

        // ---------------------------------------------
        // CHECK IMAGE URL
        // ---------------------------------------------

        if (carImage == null ||
                carImage.getImageUrl() == null ||
                carImage.getImageUrl().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Image URL is required.");
        }

        // ---------------------------------------------
        // CONNECT IMAGE TO CAR
        // ---------------------------------------------

        carImage.setCar(car);

        CarImage savedImage =
                carImageRepository.save(carImage);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedImage);
    }

    // =====================================================
    // DELETE IMAGE
    // =====================================================

    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteImage(
            @PathVariable Long imageId,
            Authentication authentication) {

        // ---------------------------------------------
        // CHECK LOGIN
        // ---------------------------------------------

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Please login first.");
        }

        // ---------------------------------------------
        // FIND IMAGE
        // ---------------------------------------------

        CarImage image =
                carImageRepository.findById(imageId)
                        .orElse(null);

        if (image == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Image not found.");
        }

        // ---------------------------------------------
        // DELETE
        // ---------------------------------------------

        carImageRepository.delete(image);

        return ResponseEntity.ok(
                "Image deleted successfully."
        );
    }
}