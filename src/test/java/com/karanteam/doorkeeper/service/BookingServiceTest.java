package com.karanteam.doorkeeper.service;

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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class BookingServiceTest {

    private static final String USER_ID = "USER_ID";

    @Autowired
    private BookingService bookingService;

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private BookingCacheService bookingCacheService;

    private static final Booking waitingBooking = Booking.builder()
        .userId(USER_ID).ordinal(1).build();
    private static final Booking activeBooking = Booking.builder()
        .userId(USER_ID).ordinal(1).entered(true).build();
    private static final Booking closedBooking = Booking.builder()
        .userId(USER_ID).ordinal(1).entered(true).exited(true).build();

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
        verify(bookingRepository).save(closedBooking);
    }

    @Test
    public void callingEntryWithNotRegisteredUserShouldThrowException() {
        when(bookingRepository.findByEnteredAndUserId(false, USER_ID)).thenReturn(Optional.empty());
        Assertions.assertThrows(EntryNotFoundException.class, () -> bookingService.entry(USER_ID));
    }

    @Test
    public void callingEntryForWaitingQueueShouldThrowException() {
        when(bookingRepository.findByEnteredAndUserId(false, USER_ID))
            .thenReturn(Optional.of(waitingBooking));
        when(bookingCacheService.calculatePositionFromOrdinal(anyInt())).thenReturn(1);
        Assertions.assertThrows(EntryForbiddenException.class, () -> bookingService.entry(USER_ID));
    }

    @Test
    public void callingEntryForAcceptedUserShouldActiveBooking() {
        when(bookingRepository.findByEnteredAndUserId(false, USER_ID))
            .thenReturn(Optional.of(waitingBooking));
        when(bookingCacheService.calculatePositionFromOrdinal(anyInt())).thenReturn(0);
        bookingService.entry(USER_ID);
        verify(bookingRepository).save(activeBooking);
    }

    @Test
    public void callingRegisterShouldSaveBooking() {
        when(bookingRepository.findByEnteredAndUserId(false, USER_ID)).thenReturn(Optional.empty());
        when(bookingRepository.save(any())).thenReturn(waitingBooking);
        when(bookingCacheService.calculatePositionFromOrdinal(anyInt())).thenReturn(7);

        RegisterResponse response = bookingService.register(USER_ID);

        verify(bookingRepository).save(Booking.builder().userId(USER_ID).build());

        Assertions.assertEquals(new RegisterResponse().canEnter(false).position(7), response);
    }

    @Test
    public void callingRegisterOnAlreadyRegisteredUserShouldNotSaveBookingAgain() {
        when(bookingRepository.findByEnteredAndUserId(false, USER_ID))
            .thenReturn(Optional.of(waitingBooking));
        when(bookingRepository.save(any())).thenReturn(waitingBooking);
        when(bookingCacheService.calculatePositionFromOrdinal(anyInt())).thenReturn(7);

        RegisterResponse response = bookingService.register(USER_ID);

        verify(bookingRepository, times(0)).save(any());

        Assertions.assertEquals(new RegisterResponse().canEnter(false).position(7), response);
    }

    @Test
    public void callingStatusShouldResponseStatusValue() {
        when(bookingRepository.findByEnteredAndUserId(false, USER_ID))
            .thenReturn(Optional.of(waitingBooking));
        when(bookingCacheService.calculatePositionFromOrdinal(anyInt())).thenReturn(7);
        StatusResponse status = bookingService.status(USER_ID);
        Assertions.assertEquals(new StatusResponse().position(7), status);
    }

    @Test
    public void callingStatusOnNotRegisteredUserShouldThrowException() {
        when(bookingRepository.findByEnteredAndUserId(false, USER_ID))
            .thenReturn(Optional.empty());
        Assertions.assertThrows(EntryNotFoundException.class, () -> bookingService.status(USER_ID));
    }

}
