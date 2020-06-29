package com.karanteam.doorkeeper.repository;

import com.karanteam.doorkeeper.entity.OfficePosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfficePositionsRepository extends JpaRepository<OfficePosition, Integer> {
}
