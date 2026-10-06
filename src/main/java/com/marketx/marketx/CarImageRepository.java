package com.marketx.marketx;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CarImageRepository
        extends JpaRepository<CarImage, Long> {

    List<CarImage> findByCarId(Long carId);
}