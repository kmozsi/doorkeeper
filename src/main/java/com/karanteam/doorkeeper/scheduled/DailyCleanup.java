package com.karanteam.doorkeeper.scheduled;

import com.karanteam.doorkeeper.service.BookingService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Scheduled service to clean bookings for users that did not enter the office during the day. The
 * cleanup runs every day at midnight.
 */
@Configuration
@EnableScheduling
public class DailyCleanup {

  private final BookingService bookingService;

  public DailyCleanup(BookingService bookingService) {
    this.bookingService = bookingService;
  }

  @Scheduled(cron = "0 0 0 * * ?")
  public void cleanupBookings() {
    bookingService.cleanupBookings();
  }

}
