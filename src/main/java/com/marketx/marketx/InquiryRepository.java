package com.marketx.marketx;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    List<Inquiry> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    List<Inquiry> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
}