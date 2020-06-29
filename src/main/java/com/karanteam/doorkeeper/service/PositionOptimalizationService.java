package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.entity.OfficePosition;
import org.paukov.combinatorics3.Generator;
import org.paukov.combinatorics3.IGenerator;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PositionOptimalizationService {

    public List<OfficePosition> getOptimalPositionDistributionWithTraverse(
        double minimumDistanceInPixels,
        List<OfficePosition> allPositions
    ) {
        return traverse(allPositions, new ArrayList<>(), minimumDistanceInPixels)
            .stream()
            .map(allPositions::get)
            .collect(Collectors.toList());
    }


    private List<Integer> traverse(List<OfficePosition> all, List<Integer> currentState, double minimumDistanceInPixels) {
        String string = currentState.stream().map(Object::toString).collect(Collectors.joining(","));
        System.out.println("State (size:" + currentState.size() + "):" + string);

        int startIndex = 0;
        if (currentState.size() > 0) {
            startIndex = currentState.get(currentState.size() - 1) + 1;
        }

        List<Integer> nextPositions = getAcceptableNextPositions(startIndex, all, minimumDistanceInPixels);

        List<Integer>[] newBestState = new List[]{currentState};
        nextPositions.stream().forEach(
            position -> {
                List<Integer> newState = new ArrayList<>(currentState);
                newState.add(position);
                List<Integer> stateToExamine = traverse(all, newState, minimumDistanceInPixels);
                if (stateToExamine.size() > newBestState[0].size()) {
                    newBestState[0] = stateToExamine;
                }
            }
        );

        return newBestState[0];
    }

    private List<Integer> getAcceptableNextPositions(int startIndex, List<OfficePosition> allPositions, double minimumDistanceInPixels) {
        return IntStream.range(startIndex, allPositions.size()).boxed().collect(Collectors.toList());
    }

//    private List<List<Integer>> getNextSteps(List<Integer> state, List<OfficePosition> all) {
//        int startIndex = 0;
//
//        if (state.size() > 0) {
//            startIndex = state.get(state.size() - 1) + 1;
//        }
//
//        List<Integer> optionalNextPositions = getAcceptableNextPositions(startIndex, all, minimumDistanceInPixels);
//
//        return optionalNextPositions.stream().map(nextIndex -> {
//            List<Integer> newList = new ArrayList<>(state);
//            newList.add(nextIndex);
//            return newList;
//        }).collect(Collectors.toList());
//    }



    public List<OfficePosition> getOptimalPositionDistribution(
        double minimumDistanceInPixels,
        List<OfficePosition> allPositions
    ) {
        int count = allPositions.size();
        int bookableCount = allPositions.size();
        
        allPositions.sort(Comparator.comparingInt(OfficePosition::getX));
        int minX = allPositions.get(0).getX();
        int maxX = allPositions.get(count - 1).getX();

        allPositions.sort(Comparator.comparingInt(OfficePosition::getY));
        int minY = allPositions.get(0).getY();
        int maxY = allPositions.get(count - 1).getY();

        int distance = (int)minimumDistanceInPixels;

        List<List<OfficePosition>> groups = new ArrayList<>();

        IntStream.range(minX - distance, maxX + distance).filter(x -> x % minimumDistanceInPixels == 0).forEach(
            x -> {
                IntStream.range(minY - distance, maxY + distance).filter(y -> y % minimumDistanceInPixels == 0).forEach(
                    y -> {
                        groups.add(putPositionsToGroup(allPositions, x, y, distance));
                    }
                );
            }
        );

        System.out.println(groups);
        
        

        while (bookableCount > 0) {
            System.out.println("try bookable count:" + bookableCount);
            Optional<List<OfficePosition>> validDistribution = getValidDistribution(allPositions, bookableCount, minimumDistanceInPixels);
            if (validDistribution.isPresent()) {
                return validDistribution.get();
            }
            bookableCount = bookableCount - 1;
        }

        return null;
    }

    private List<OfficePosition> putPositionsToGroup(List<OfficePosition> allPositions, int x, int y, int minDist) {

        int minX = x;
        int maxX = x + 2 * minDist;
        int minY = y;
        int maxY = y + 2 * minDist;

        System.out.println("TO GROUP: X: " + minX + "-" + maxX + "   Y: " + minY + "-" + maxY);

        return allPositions.stream().filter(
            pos -> pos.getX() > minX && pos.getX() < maxX && pos.getY() > minY && pos.getY() < maxY
        ).collect(Collectors.toList());
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
