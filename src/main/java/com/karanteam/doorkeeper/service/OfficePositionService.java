package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.config.CapacityConfig;
import com.karanteam.doorkeeper.entity.OfficePosition;
import com.karanteam.doorkeeper.enumeration.PositionStatus;
import com.karanteam.doorkeeper.repository.OfficePositionsRepository;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OfficePositionService {

    private final OfficePositionsRepository officePositionsRepository;
    private final CapacityConfig capacityConfig;
    private final AdminService adminService;

    public OfficePositionService(
        OfficePositionsRepository officePositionsRepository,
        CapacityConfig capacityConfig, AdminService adminService) {
        this.officePositionsRepository = officePositionsRepository;
        this.capacityConfig = capacityConfig;
        this.adminService = adminService;
    }

    public void setPositions(List<OfficePosition> officePositions) {
        officePositionsRepository.saveAll(officePositions);
    }

    public Optional<OfficePosition> findByUserId(final String userId) {
        return officePositionsRepository.findByUserId(userId);
    }

    public Optional<OfficePosition> findById(final Integer positionId) {
        return officePositionsRepository.findById(positionId);
    }

    public void enter(final String userId) {
        changeStatus(userId, PositionStatus.TAKEN);
    }

    public void exit(final String userId) {
        changeStatus(userId, PositionStatus.FREE);
    }

    private void changeStatus(final String userId, final PositionStatus status) {
        officePositionsRepository.findByUserId(userId).ifPresent(pos -> {
            pos.setStatus(status);
            officePositionsRepository.save(pos);
        });
        // TODO modify the picture of the actual status
    }

    public synchronized OfficePosition getNextFreePosition(final String userId) {
        final List<OfficePosition> all = officePositionsRepository.findAll();
        final List<OfficePosition> free =
            all.stream().filter(pos -> pos.getStatus() == PositionStatus.FREE)
                .collect(Collectors.toList());
        final List<OfficePosition> taken =
            all.stream().filter(pos -> pos.getStatus() != PositionStatus.FREE)
                .collect(Collectors.toList());

        for (OfficePosition pos : free) {
            boolean available = true;
            for (OfficePosition takenPos : taken) {
                if (!isDistanceAllowed(pos, takenPos)) {
                    available = false;
                    break;
                }
            }
            if (available) {
                pos.setStatus(PositionStatus.BOOKED);
                pos.setUserId(userId);
                officePositionsRepository.save(pos);
                return pos;
            }
        }
        return null;
    }

    private boolean isDistanceAllowed(OfficePosition position, OfficePosition otherPosition) {
        return position.distanceFrom(otherPosition) * capacityConfig.getPixelSizeInCm()
            >= adminService.getActualMinimalDistance() * 100;
    }
}
