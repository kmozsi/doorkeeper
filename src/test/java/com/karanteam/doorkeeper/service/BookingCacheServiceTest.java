package com.karanteam.doorkeeper.service;

import static com.karanteam.doorkeeper.data.OfficePositionOrientation.NORTH;
import static com.karanteam.doorkeeper.enumeration.PositionStatus.BOOKED;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.karanteam.doorkeeper.config.CachingConfig;
import com.karanteam.doorkeeper.entity.Booking;
import com.karanteam.doorkeeper.entity.OfficePosition;
import com.karanteam.doorkeeper.exception.EntryNotFoundException;
import com.karanteam.doorkeeper.repository.BookingRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;

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
    private OfficePositionService officePositionService;

    @MockBean
    private AdminService adminService;

    private static final Booking activeBooking = Booking.builder()
        .userId(USER_ID).ordinal(1).entered(true).officePosition(
            OfficePosition.builder().id(1).orientation(NORTH).x(1).y(1).status(BOOKED).build()
        ).build();
    private static final Booking closedBooking = Booking.builder()
        .userId(USER_ID).ordinal(1).entered(true).exited(true).officePosition(
            OfficePosition.builder().id(1).orientation(NORTH).x(1).y(1).status(BOOKED).build()
        ).build();

    @Autowired
    CacheManager cacheManager;

    private void evictCache() {
        cacheManager.getCache(CachingConfig.POSITION_CACHE).clear();
        cacheManager.getCache(CachingConfig.VIP_CACHE).clear();
    }

    @Test
    public void testCacheWorking() {
        evictCache();

        int exitedUsers = 3;
        int capacity = 10;
        int ordinal = 12;

        when(adminService.getActualDailyCapacity()).thenReturn(capacity);
        when(bookingRepository.findTopByOrderByOrdinalAsc())
            .thenReturn(Booking.builder().ordinal(1).build());
        when(bookingRepository.countAllByExitedAndOrdinalLessThan(true, ordinal))
            .thenReturn(exitedUsers);

        bookingCacheService.calculatePositionFromOrdinal(ordinal);
        bookingCacheService.calculatePositionFromOrdinal(ordinal);
        bookingCacheService.calculatePositionFromOrdinal(ordinal);

        verify(adminService, times(1)).getActualDailyCapacity();
        verify(bookingRepository, times(1)).findTopByOrderByOrdinalAsc();
        verify(bookingRepository, times(1)).countAllByExitedAndOrdinalLessThan(true, ordinal);
    }

    @Test
    public void calculatePositionWhenUserWaiting() {
        evictCache();

        int exitedUsers = 0;
        int capacity = 10;
        int ordinal = 12;

        when(adminService.getActualDailyCapacity()).thenReturn(capacity);
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

        when(adminService.getActualDailyCapacity()).thenReturn(capacity);
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

        when(adminService.getActualDailyCapacity()).thenReturn(capacity);
        when(bookingRepository.findTopByOrderByOrdinalAsc())
            .thenReturn(Booking.builder().ordinal(1).build());
        when(bookingRepository.countAllByExitedAndOrdinalLessThan(true, ordinal))
            .thenReturn(exitedUsers);

        Assertions.assertEquals(0, bookingCacheService.calculatePositionFromOrdinal(ordinal));
    }

    @Test
    public void callingExitWithUserNotInBuildingShouldThrowException() {
        when(bookingRepository.findByExitedAndEnteredAndUserId(anyBoolean(), anyBoolean(), anyString()))
            .thenReturn(Optional.empty());
        Assertions.assertThrows(EntryNotFoundException.class, () -> bookingService.exit(USER_ID));
    }

    @Test
    public void callingExitShouldCloseBooking() {
        when(bookingRepository.findByExitedAndEnteredAndUserId(anyBoolean(), anyBoolean(), anyString()))
            .thenReturn(Optional.of(activeBooking));
        bookingService.exit(USER_ID);
        verify(officePositionService).exit(closedBooking.getOfficePosition());
        verify(bookingRepository).save(argThat(booking ->
            booking.getUserId().equals(USER_ID) && booking.isExited() && booking.getOfficePosition() == null)
        );
    }

}
