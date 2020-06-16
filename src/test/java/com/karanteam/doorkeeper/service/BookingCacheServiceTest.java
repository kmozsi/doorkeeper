package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.config.CachingConfig;
import com.karanteam.doorkeeper.entity.Booking;
import com.karanteam.doorkeeper.exception.EntryForbiddenException;
import com.karanteam.doorkeeper.exception.EntryNotFoundException;
import com.karanteam.doorkeeper.repository.BookingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openapitools.model.RegisterResponse;
import org.openapitools.model.StatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.annotation.Cacheable;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class BookingCacheServiceTest {

    private static final String USER_ID = "USER_ID";

    @Autowired
    private BookingCacheService bookingCacheService;

    @Autowired
    private BookingService bookingService;

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private OfficeCapacityService officeCapacityService;

    private static final Booking activeBooking = Booking.builder()
        .userId(USER_ID).ordinal(1).entered(true).build();

    private void evictCache() {
        when(bookingRepository.findByExitedAndEnteredAndUserId(anyBoolean(), anyBoolean(), anyString()))
            .thenReturn(Optional.of(activeBooking));
        bookingService.exit(USER_ID);
    }

    @Test
    public void testCacheWorking() {
        evictCache();

        int exitedUsers = 3;
        int capacity = 10;
        int ordinal = 12;

        when(officeCapacityService.getActualDailyCapacity()).thenReturn(capacity);
        when(bookingRepository.findTopByOrderByOrdinalAsc())
            .thenReturn(Booking.builder().ordinal(1).build());
        when(bookingRepository.countAllByExitedAndOrdinalLessThan(true, ordinal))
            .thenReturn(exitedUsers);

        bookingCacheService.calculatePositionFromOrdinal(ordinal);
        bookingCacheService.calculatePositionFromOrdinal(ordinal);
        bookingCacheService.calculatePositionFromOrdinal(ordinal);

        verify(officeCapacityService, times(1)).getActualDailyCapacity();
        verify(bookingRepository, times(1)).findTopByOrderByOrdinalAsc();
        verify(bookingRepository, times(1)).countAllByExitedAndOrdinalLessThan(true, ordinal);
    }

    @Test
    public void calculatePositionWhenUserWaiting() {
        evictCache();

        int exitedUsers = 0;
        int capacity = 10;
        int ordinal = 12;

        when(officeCapacityService.getActualDailyCapacity()).thenReturn(capacity);
        when(bookingRepository.findTopByOrderByOrdinalAsc())
            .thenReturn(Booking.builder().ordinal(1).build());
        when(bookingRepository.countAllByExitedAndOrdinalLessThan(true, ordinal))
            .thenReturn(exitedUsers);

        Assertions.assertEquals(2, bookingCacheService.calculatePositionFromOrdinal(ordinal));
    }

    @Test
    public void calculatePositionWhenUserWaitingAndSomeoneExited() {
        evictCache();

        int exitedUsers = 1;
        int capacity = 10;
        int ordinal = 12;

        when(officeCapacityService.getActualDailyCapacity()).thenReturn(capacity);
        when(bookingRepository.findTopByOrderByOrdinalAsc())
            .thenReturn(Booking.builder().ordinal(1).build());
        when(bookingRepository.countAllByExitedAndOrdinalLessThan(true, ordinal))
            .thenReturn(exitedUsers);

        Assertions.assertEquals(1, bookingCacheService.calculatePositionFromOrdinal(ordinal));
    }

    @Test
    public void calculatePositionWhenUserCanEntry() {
        evictCache();

        int exitedUsers = 3;
        int capacity = 10;
        int ordinal = 12;

        when(officeCapacityService.getActualDailyCapacity()).thenReturn(capacity);
        when(bookingRepository.findTopByOrderByOrdinalAsc())
            .thenReturn(Booking.builder().ordinal(1).build());
        when(bookingRepository.countAllByExitedAndOrdinalLessThan(true, ordinal))
            .thenReturn(exitedUsers);

        Assertions.assertEquals(0, bookingCacheService.calculatePositionFromOrdinal(ordinal));
    }


}
