package com.karanteam.doorkeeper.repository;

import com.karanteam.doorkeeper.entity.Booking;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    Integer countAllByExited(boolean exited);

    Integer countAllByExitedAndOrdinalLessThan(boolean exited, int ordinal);

    Booking findTopByOrderByOrdinalAsc();

    Optional<Booking> findByEnteredAndUserId(boolean entered, String userId);

    Optional<Booking> findByExitedAndEnteredAndUserId(boolean exited, boolean entered, String userId);

    List<Booking> findByEntered(boolean entered);
}
