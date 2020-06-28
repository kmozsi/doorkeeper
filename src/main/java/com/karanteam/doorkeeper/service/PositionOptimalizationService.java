package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.entity.OfficePosition;
import org.paukov.combinatorics3.Generator;
import org.paukov.combinatorics3.IGenerator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
public class PositionOptimalizationService {

    public List<OfficePosition> getOptimalPositionDistribution(
        double minimumDistanceInPixels,
        List<OfficePosition> allPositions
    ) {
//        int bookableCount = allPositions.size();

        int bookableCount = allPositions.size();

        while (bookableCount > 0) {
            System.out.println("try bookable count:" + bookableCount);
            Optional<List<OfficePosition>> validDistribution = getValidDistribution(allPositions, bookableCount, minimumDistanceInPixels);
            if (validDistribution.isPresent()) {
                return validDistribution.get();
            }
            bookableCount = bookableCount - 1;
        }

        return null;


//        return IntStream.range(0, availablePositions.size()).mapToObj(
//            index -> {
//                int bookableCount = availablePositions.size() - index;
//                return getValidDistribution(availablePositions, bookableCount);
////                if (validDistribution.isPresent()) {
////                    return validDistribution.get();
////                }
//            }).filter(list -> list.isPresent()).findFirst().get().get();
    }

    private Optional<List<OfficePosition>> getValidDistribution(
        List<OfficePosition> availablePositions,
        int bookableCount,
        double minimumDistanceInPixels
    ) {
        System.out.println("(" + availablePositions.size() + "," + bookableCount + ") --");
        IGenerator<List<OfficePosition>> combinations = Generator.combination(availablePositions).simple(bookableCount);
        System.out.println("(" + availablePositions.size() + "," + bookableCount + ")");
        System.out.println("(" + availablePositions.size() + "," + bookableCount + ") -> " + combinations.stream().count());
        return combinations
            .stream()
            .filter(list -> isValidDistribution(list, minimumDistanceInPixels)).findFirst();
    }

    private boolean isValidDistribution(List<OfficePosition> list, double minimumDistanceInPixels) {
        return IntStream.range(0, list.size()).allMatch(
            index -> IntStream.range(index + 1, list.size()).allMatch(
                index2 -> isDistanceAllowed(list.get(index), list.get(index2), minimumDistanceInPixels)
            )
        );
    }

    private boolean isDistanceAllowed(OfficePosition position, OfficePosition otherPosition, double minimumDistanceInPixels) {
        return position.distanceFrom(otherPosition) > minimumDistanceInPixels;
    }
}
