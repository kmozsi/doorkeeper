package com.bigtv.doorkeeper.service;

import com.bigtv.doorkeeper.repository.OfficeCapacityRepository;
import org.springframework.stereotype.Service;

@Service
public class OfficeCapacityService {

    public int getActualDailyCapacity() {
        return 2;
    }
}
