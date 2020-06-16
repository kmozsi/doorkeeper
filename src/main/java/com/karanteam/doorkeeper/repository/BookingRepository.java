package com.karanteam.doorkeeper.repository;

import com.karanteam.doorkeeper.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    Integer countAllByExitedAndOrdinalLessThan(boolean exited, int ordinal);

    Booking findTopByOrderByOrdinalAsc();

    Optional<Booking> findByEnteredAndUserId(boolean entered, String userId);
    Optional<Booking> findByExitedAndEnteredAndUserId(boolean exited, boolean entered, String userId);
}
