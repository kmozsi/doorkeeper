package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.config.MessagingConfig;
import com.karanteam.doorkeeper.entity.Booking;
import com.karanteam.doorkeeper.entity.OfficePosition;
import com.karanteam.doorkeeper.enumeration.PositionStatus;
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
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.karanteam.doorkeeper.data.OfficePositionOrientation.NORTH;
import static com.karanteam.doorkeeper.enumeration.PositionStatus.BOOKED;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class BookingServiceTest {

    private static final String USER_ID = "USER_ID";
    private static final String USER_ID2 = "USER_ID2";

    @Autowired
    private BookingService bookingService;

    @MockBean
    private VipService vipService;

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private OfficePositionService officePositionService;

    @MockBean
    private MessagingService messagingService;

    @MockBean
    private BookingCacheService bookingCacheService;

    @SpyBean
    private MessagingConfig messagingConfig;

    private static final OfficePosition FREE_POSITION = OfficePosition.builder()
        .id(1).orientation(NORTH).status(PositionStatus.FREE).build();

    private static final Booking waitingBooking = Booking.builder()
        .userId(USER_ID).ordinal(1).officePosition(
            OfficePosition.builder().id(1).status(BOOKED).orientation(NORTH).x(1).y(1).build()
        ).build();
    private static final Booking waitingForPositionBooking = Booking.builder()
        .userId(USER_ID).ordinal(1).build();
    private static final Booking activeBooking = Booking.builder()
        .userId(USER_ID).ordinal(1).entered(true).officePosition(
            OfficePosition.builder().id(1).status(BOOKED).orientation(NORTH).x(1).y(1).build()
        ).build();

    @Test
    public void callingExitWithVipUser() {
        when(vipService.isVip(anyString())).thenReturn(true);

        bookingService.exit(USER_ID);

        verify(bookingCacheService, never()).exit(anyString());
    }

    @Test
    public void callingExitWithRegularUser() {
        when(vipService.isVip(anyString())).thenReturn(false);
        when(bookingRepository.findAll()).thenReturn(Collections.emptyList());

        bookingService.exit(USER_ID);

        verify(bookingCacheService, times(1)).exit(USER_ID);
        verify(officePositionService, never()).getNextFreePosition();
        verify(bookingRepository, never()).save(any());
        verify(messagingService, never()).sendMessage(any());
    }

    @Test
    public void callingExitWithRegularUserThenNotify() {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(Booking.builder().ordinal(0).userId(USER_ID).build());
        bookings.add(Booking.builder().ordinal(1).userId(USER_ID2).build());

        when(bookingCacheService.calculatePositionFromOrdinal(0)).thenReturn(0);
        when(bookingCacheService.calculatePositionFromOrdinal(1)).thenReturn(1);
        when(vipService.isVip(anyString())).thenReturn(false);
        when(bookingRepository.findAll()).thenReturn(bookings);
        when(messagingConfig.getNotifyPosition()).thenReturn(1);
        doNothing().when(messagingService).sendMessage(anyString());

        bookingService.exit(USER_ID);

        verify(bookingCacheService, times(1)).exit(USER_ID);
        verify(messagingService, times(1)).sendMessage(USER_ID2);
    }

    @Test
    public void callingExitWithRegularUserAndSomeoneWaiting() {
        when(vipService.isVip(anyString())).thenReturn(false);
        when(bookingRepository.findAll()).thenReturn(Collections.singletonList(waitingForPositionBooking));
        when(officePositionService.getNextFreePosition()).thenReturn(FREE_POSITION);

        bookingService.exit(USER_ID);

        verify(bookingCacheService, times(1)).exit(USER_ID);
        verify(officePositionService).getNextFreePosition();
        verify(bookingRepository).save(argThat(booking -> booking.getOfficePosition().equals(FREE_POSITION)));
        verify(messagingService, never()).sendMessage(any());
    }

    @Test
    public void callingEntryWithNotRegisteredUserShouldThrowException() {
        when(vipService.isVip(anyString())).thenReturn(false);
        when(bookingRepository.findByEnteredAndUserId(false, USER_ID)).thenReturn(Optional.empty());
        Assertions.assertThrows(EntryNotFoundException.class, () -> bookingService.entry(USER_ID));
    }

    @Test
    public void callingEntryForWaitingQueueShouldThrowException() {
        when(vipService.isVip(anyString())).thenReturn(false);
        when(bookingRepository.findByEnteredAndUserId(false, USER_ID))
            .thenReturn(Optional.of(waitingBooking));
        when(bookingCacheService.calculatePositionFromOrdinal(anyInt())).thenReturn(1);
        Assertions.assertThrows(EntryForbiddenException.class, () -> bookingService.entry(USER_ID));
    }

    @Test
    public void callingEntryForAcceptedUserShouldActiveBooking() {
        when(vipService.isVip(anyString())).thenReturn(false);
        when(bookingRepository.findByEnteredAndUserId(false, USER_ID))
            .thenReturn(Optional.of(waitingBooking));
        when(bookingCacheService.calculatePositionFromOrdinal(anyInt())).thenReturn(0);
        bookingService.entry(USER_ID);
        verify(bookingRepository).save(activeBooking);
        verify(officePositionService).enter(activeBooking.getOfficePosition());
    }

    @Test
    public void callingEntryWithVipUser() {
        when(vipService.isVip(anyString())).thenReturn(true);
        bookingService.entry(USER_ID);
        verifyNoInteractions(bookingRepository);
        verifyNoInteractions(bookingCacheService);
    }

    @Test
    public void callingRegisterShouldSaveBooking() {
        when(vipService.isVip(anyString())).thenReturn(false);
        when(bookingRepository.findByEnteredAndUserId(false, USER_ID)).thenReturn(Optional.empty());
        when(bookingRepository.save(any())).thenReturn(waitingBooking);
        when(bookingCacheService.calculatePositionFromOrdinal(anyInt())).thenReturn(7);
        when(officePositionService.getNextFreePosition()).thenReturn(FREE_POSITION);

        RegisterResponse response = bookingService.register(USER_ID);

        verify(bookingRepository).save(Booking.builder().userId(USER_ID).officePosition(
            FREE_POSITION
        ).build());

        Assertions.assertEquals(new RegisterResponse().canEnter(false).position(7).positionPicture("http://localhost/positions/1"), response);
    }

    @Test
    public void callingRegisterOnAlreadyRegisteredUserShouldNotSaveBookingAgain() {
        when(vipService.isVip(anyString())).thenReturn(false);
        when(bookingRepository.findByEnteredAndUserId(false, USER_ID))
            .thenReturn(Optional.of(waitingBooking));
        when(bookingRepository.save(any())).thenReturn(waitingBooking);
        when(bookingCacheService.calculatePositionFromOrdinal(anyInt())).thenReturn(7);

        RegisterResponse response = bookingService.register(USER_ID);

        verify(bookingRepository, times(0)).save(any());

        Assertions.assertEquals(new RegisterResponse().canEnter(false).position(7).positionPicture("http://localhost/positions/1"), response);
    }

    @Test
    public void callingRegisterWithVipUser() {
        when(vipService.isVip(anyString())).thenReturn(true);
        RegisterResponse response = bookingService.register(USER_ID);
        verifyNoInteractions(bookingRepository);
        verifyNoInteractions(bookingCacheService);
        Assertions.assertEquals(new RegisterResponse().canEnter(true).position(0), response);
    }

    @Test
    public void callingStatusShouldResponseStatusValue() {
        when(vipService.isVip(anyString())).thenReturn(false);
        when(bookingRepository.findByEnteredAndUserId(false, USER_ID))
            .thenReturn(Optional.of(waitingBooking));
        when(bookingCacheService.calculatePositionFromOrdinal(anyInt())).thenReturn(7);
        StatusResponse status = bookingService.status(USER_ID);
        Assertions.assertEquals(new StatusResponse().position(7).positionPicture("http://localhost/positions/1"), status);
    }

    @Test
    public void callingStatusOnNotRegisteredUserShouldThrowException() {
        when(vipService.isVip(anyString())).thenReturn(false);
        when(bookingRepository.findByEnteredAndUserId(false, USER_ID))
            .thenReturn(Optional.empty());
        Assertions.assertThrows(EntryNotFoundException.class, () -> bookingService.status(USER_ID));
    }

    @Test
    public void callingStatusWithVipUser() {
        when(vipService.isVip(anyString())).thenReturn(true);
        StatusResponse response = bookingService.status(USER_ID);
        verifyNoInteractions(bookingRepository);
        verifyNoInteractions(bookingCacheService);
        Assertions.assertEquals(new StatusResponse().position(0), response);
    }
}
