package com.karanteam.doorkeeper.repository;

import com.karanteam.doorkeeper.entity.OfficeCapacity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OfficeCapacityRepository extends JpaRepository<OfficeCapacity, Integer> {
    Optional<OfficeCapacity> findTopByOrderByIdAsc();
}
