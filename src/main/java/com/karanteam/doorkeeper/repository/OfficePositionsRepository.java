package com.karanteam.doorkeeper.repository;

import com.karanteam.doorkeeper.entity.OfficePosition;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfficePositionsRepository extends JpaRepository<OfficePosition, Integer> {

    Optional<OfficePosition> findByUserId(final String userId);

}
