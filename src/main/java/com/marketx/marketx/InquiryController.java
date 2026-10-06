package com.marketx.marketx;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inquiries")
@CrossOrigin
public class InquiryController {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final CarRepository carRepository;

    public InquiryController(
            InquiryRepository inquiryRepository,
            UserRepository userRepository,
            CarRepository carRepository) {

        this.inquiryRepository = inquiryRepository;
        this.userRepository = userRepository;
        this.carRepository = carRepository;
    }

    // =========================
    // SEND INQUIRY
    // =========================

    @PostMapping
    public ResponseEntity<?> sendInquiry(
            @RequestBody Inquiry inquiry,
            Authentication authentication) {

        // Check login
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Please login first.");
        }

        // Find logged-in buyer
        String email = authentication.getName();

        user buyer = userRepository
                .findByEmail(email)
                .orElse(null);

        if (buyer == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User not found.");
        }

        // Check car
        if (inquiry.getCarId() == null) {
            return ResponseEntity
                    .badRequest()
                    .body("Car ID is required.");
        }

   Car existingCar = carRepository
        .findById(inquiry.getCarId())
        .orElse(null);

        if (existingCar == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Car not found.");
        }

        // Check message
        if (inquiry.getMessage() == null ||
                inquiry.getMessage().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Message cannot be empty.");
        }

        // Get seller from the car
        Long sellerId = existingCar.getSellerId();

        if (sellerId == null) {
            return ResponseEntity
                    .badRequest()
                    .body("This car does not have a seller.");
        }

        // Seller cannot contact own car
        if (sellerId.equals(buyer.getId())) {
            return ResponseEntity
                    .badRequest()
                    .body("You cannot contact yourself.");
        }

        // Set values from server
        inquiry.setBuyerId(buyer.getId());
        inquiry.setSellerId(sellerId);
        inquiry.setMessage(inquiry.getMessage().trim());
        inquiry.setStatus("NEW");
        inquiry.setCreatedAt(LocalDateTime.now());

        Inquiry savedInquiry =
                inquiryRepository.save(inquiry);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedInquiry);
    }


    // =========================
    // BUYER'S SENT INQUIRIES
    // =========================

    @GetMapping("/sent")
    public ResponseEntity<?> getSentInquiries(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Please login first.");
        }

        String email = authentication.getName();

        user buyer = userRepository
                .findByEmail(email)
                .orElse(null);

        if (buyer == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User not found.");
        }

        List<Inquiry> inquiries =
                inquiryRepository
                        .findByBuyerIdOrderByCreatedAtDesc(buyer.getId());

        return ResponseEntity.ok(inquiries);
    }


    // =========================
    // SELLER'S RECEIVED INQUIRIES
    // =========================

    @GetMapping("/received")
    public ResponseEntity<?> getReceivedInquiries(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Please login first.");
        }

        String email = authentication.getName();

        user seller = userRepository
                .findByEmail(email)
                .orElse(null);

        if (seller == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User not found.");
        }

        List<Inquiry> inquiries =
                inquiryRepository
                        .findBySellerIdOrderByCreatedAtDesc(seller.getId());

        return ResponseEntity.ok(inquiries);
    }


    // =========================
    // UPDATE INQUIRY STATUS
    // =========================

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Inquiry statusRequest,
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Please login first.");
        }

        String email = authentication.getName();

        user currentUser = userRepository
                .findByEmail(email)
                .orElse(null);

        if (currentUser == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User not found.");
        }

        Inquiry inquiry = inquiryRepository
                .findById(id)
                .orElse(null);

        if (inquiry == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Inquiry not found.");
        }

        // Only the seller can update the inquiry
        if (!currentUser.getId().equals(inquiry.getSellerId())) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("You are not allowed to update this inquiry.");
        }

        String newStatus = statusRequest.getStatus();

        if (newStatus == null ||
                newStatus.trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Status is required.");
        }

        inquiry.setStatus(newStatus.trim().toUpperCase());

        Inquiry updatedInquiry =
                inquiryRepository.save(inquiry);

        return ResponseEntity.ok(updatedInquiry);
    }
}