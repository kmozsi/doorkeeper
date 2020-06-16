package com.bigtv.doorkeeper.repository;

import com.bigtv.doorkeeper.entity.OfficeCapacity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OfficeCapacityRepository extends JpaRepository<OfficeCapacity, Integer> {
    Optional<OfficeCapacity> findTopByOrderByIdAsc();
}
