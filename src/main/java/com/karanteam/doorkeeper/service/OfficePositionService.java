package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.config.ApplicationConfig;
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
    private final ApplicationConfig applicationConfig;
    private final AdminService adminService;

    public OfficePositionService(
        OfficePositionsRepository officePositionsRepository,
        ApplicationConfig applicationConfig, AdminService adminService) {
        this.officePositionsRepository = officePositionsRepository;
        this.applicationConfig = applicationConfig;
        this.adminService = adminService;
    }

    public void setPositions(List<OfficePosition> officePositions) {
        officePositionsRepository.deleteAll();
        officePositionsRepository.saveAll(officePositions);
    }

    public Optional<OfficePosition> findById(final Integer positionId) {
        return officePositionsRepository.findById(positionId);
    }

    public void enter(final Integer id) {
        changeStatus(id, PositionStatus.TAKEN);
    }

    public void exit(final Integer id) {
        changeStatus(id, PositionStatus.FREE);
    }

    private void changeStatus(final Integer id, final PositionStatus status) {
        officePositionsRepository.findById(id).ifPresent(pos -> {
            pos.setStatus(status);
            officePositionsRepository.save(pos);
        });
        // TODO modify the picture of the actual status
    }

    public synchronized OfficePosition getNextFreePosition() {
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
                officePositionsRepository.save(pos);
                return pos;
            }
        }
        return null;
    }

    private boolean isDistanceAllowed(OfficePosition position, OfficePosition otherPosition) {
        return position.distanceFrom(otherPosition) * applicationConfig.getPixelSizeInCm()
            >= adminService.getActualMinimalDistance() * 100;
    }

    public List<OfficePosition> getAllPositions() {
        return officePositionsRepository.findAll();
    }
}
