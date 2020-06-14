package com.bigtv.doorkeeper.repository;

import com.bigtv.doorkeeper.entity.OfficeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfficeEntryRepository extends JpaRepository<OfficeEntry, String> {

    Integer countAllByEntered(boolean entered);
    Integer countAllByEnteredAndExited(boolean entered, boolean exited);
    Integer countAllByExitedAndOrdinalLessThan(boolean exited, int ordinal);
    OfficeEntry findTopByOrderByOrdinalDesc();
    OfficeEntry findTopByOrderByOrdinalAsc();

    Optional<OfficeEntry> findByEnteredAndUserId(boolean entered, String userId);
    Optional<OfficeEntry> findByExitedAndUserId(boolean exited, String userId);
}
