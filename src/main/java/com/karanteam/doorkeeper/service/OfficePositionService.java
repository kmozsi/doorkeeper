package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.config.ApplicationConfig;
import com.karanteam.doorkeeper.entity.OfficePosition;
import com.karanteam.doorkeeper.enumeration.PositionStatus;
import com.karanteam.doorkeeper.repository.OfficePositionsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OfficePositionService {

    private final OfficePositionsRepository officePositionsRepository;
    private final ApplicationConfig applicationConfig;
    private final AdminService adminService;
    private final PositionOptimalizationService positionOptimalizationService;

    public OfficePositionService(
        OfficePositionsRepository officePositionsRepository,
        ApplicationConfig applicationConfig, AdminService adminService,
        PositionOptimalizationService positionOptimalizationService
    ) {
        this.officePositionsRepository = officePositionsRepository;
        this.applicationConfig = applicationConfig;
        this.adminService = adminService;
        this.positionOptimalizationService = positionOptimalizationService;
    }

    public int setPositions(List<OfficePosition> officePositions) {
        officePositionsRepository.deleteAll();
        List<OfficePosition> availablePositions = filterAvailablePositions(officePositions);
        officePositionsRepository.saveAll(availablePositions);
        return availablePositions.size();
    }

    private List<OfficePosition> filterAvailablePositions(List<OfficePosition> allPositions) {
//        return positionOptimalizationService.getOptimalPositionDistribution(50, allPositions.subList(0,20));
         return allPositions;  // TODO !!!
//        List<OfficePosition> availablePositions = new ArrayList<>();
//
//        allPositions.forEach(pos -> {
//            if (availablePositions.stream().allMatch(available -> isDistanceAllowed(pos, available))) {
//                availablePositions.add(pos);
//            }
//        });
//        return availablePositions;
    }

    public Optional<OfficePosition> findById(final Integer positionId) {
        return officePositionsRepository.findById(positionId);
    }

    public void enter(final OfficePosition officePosition) {
        changeStatus(officePosition, PositionStatus.TAKEN);
    }

    public void exit(final OfficePosition officePosition) {
        changeStatus(officePosition, PositionStatus.FREE);
    }

    private void changeStatus(final OfficePosition officePosition, final PositionStatus status) {
        officePosition.setStatus(status);
        officePositionsRepository.save(officePosition);
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
