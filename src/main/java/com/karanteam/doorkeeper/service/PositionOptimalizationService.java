package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.entity.OfficePosition;
import org.paukov.combinatorics3.Generator;
import org.paukov.combinatorics3.IGenerator;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class PositionOptimalizationService {


    private static int prev = -1;
    private static List<List<Integer>> groups = new ArrayList<>();

    public List<OfficePosition> getOptimalPositionDistributionWithExclusionLists(
        double minDist,
        List<OfficePosition> allPositions
    ) {

        List<List<OfficePosition>> exclusionList = allPositions.stream().map(
            officePosition -> allPositions.stream().filter(other -> other.distanceFrom(officePosition) < minDist).collect(Collectors.toList())
        ).collect(Collectors.toList());

        exclusionList.sort((b,a) -> a.size() - b.size());

        return traverseExclusion(exclusionList, 0, allPositions, new ArrayList<>());
    }

    private List<OfficePosition> traverseExclusion(final List<List<OfficePosition>> exclusionList, int exclusionIndex, final List<OfficePosition> remainedPositions, final List<OfficePosition> selection) {

        if (exclusionIndex < 10) {
            System.out.println(exclusionIndex);
        }

        if (exclusionIndex >= exclusionList.size()) {
            return selection;
        }

        List<OfficePosition> currentExclusion = exclusionList.get(exclusionIndex);

        List<OfficePosition> positionToExamineInCurrentExclusion = currentExclusion
            .stream()
            .filter(remainedPositions::contains).collect(Collectors.toList());

        if (positionToExamineInCurrentExclusion.size() < 1) {
            return traverseExclusion(
                exclusionList,
                exclusionIndex + 1,
                remainedPositions,
                selection
            );
        }

        List<List<OfficePosition>> collect = positionToExamineInCurrentExclusion
            .stream()
            .map(
                selectedPosition -> {

                    ArrayList<OfficePosition> newSelection = new ArrayList<>(selection);
                    newSelection.add(selectedPosition);

                    return traverseExclusion(
                        exclusionList,
                        exclusionIndex + 1,
                        remainedPositions.stream().filter(pos -> !currentExclusion.contains(pos)).collect(Collectors.toList()),
                        newSelection
                    );
                }
            ).collect(Collectors.toList());

        collect.sort((a,b) -> b.size() - a.size());

        if (collect.size() > 0 && collect.get(0).size() > selection.size()) {
            return collect.get(0);
        }

        return selection;
    }

    public List<OfficePosition> getOptimalPositionDistributionWithTraverse(
        double minimumDistanceInPixels,
        List<OfficePosition> allPositions
    ) {

        int count = allPositions.size();

        allPositions.sort(Comparator.comparingInt(OfficePosition::getX));
        int minX = allPositions.get(0).getX();
        int maxX = allPositions.get(count - 1).getX();

        allPositions.sort(Comparator.comparingInt(OfficePosition::getY));
        int minY = allPositions.get(0).getY();
        int maxY = allPositions.get(count - 1).getY();

        int distance = (int)minimumDistanceInPixels;
        int groupSize = distance * 1;
        int delta = groupSize;

        IntStream.range(minX - groupSize, maxX + groupSize).filter(x -> x % groupSize == (groupSize / 2)).forEach(
            x -> IntStream.range(minY - groupSize, maxY + groupSize).filter(y -> y % groupSize == (groupSize / 2)).forEach(
                y -> groups.add(putPositionsToGroup(allPositions, x, y, delta))
            )
        );

        return traverse(allPositions, new ArrayList<>(), minimumDistanceInPixels)
            .stream()
            .map(allPositions::get)
            .collect(Collectors.toList());
    }


    private List<Integer> traverse(List<OfficePosition> all, List<Integer> currentState, double minimumDistanceInPixels) {
        try {
            if (currentState.get(0) != prev) {
                prev = currentState.get(0);
                System.out.println(prev);
            }
        } catch (Exception e) {
            System.out.println("NULL");
        }

        int startIndex = 0;
        if (currentState.size() > 0) {
            startIndex = currentState.get(currentState.size() - 1) + 1;
        }

        List<Integer> nextPositions = getAcceptableNextPositions(startIndex, all, currentState, minimumDistanceInPixels);

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

    private List<Integer> getAcceptableNextPositions(int startIndex, List<OfficePosition> allPositions, List<Integer> state, double minimumDistanceInPixels) {
        List<Integer> indexHaveToBeBigger = IntStream.range(startIndex, allPositions.size()).boxed().collect(Collectors.toList());

        Set<Integer> indicesInTheSameGroup = groups.stream()
            .filter(group -> group.contains(startIndex))
            .flatMap(List::stream)
            .filter(indexHaveToBeBigger::contains)
            .collect(Collectors.toSet());

        List<Integer> suggestedNextPositions = indicesInTheSameGroup
            .stream()
            .filter(index -> hasEnoughDistance(allPositions, state, minimumDistanceInPixels, index))
            .collect(Collectors.toList());

        if (suggestedNextPositions.size() > 0) {
            return suggestedNextPositions;
        }

        return indexHaveToBeBigger
            .stream()
            .filter(index -> hasEnoughDistance(allPositions, state, minimumDistanceInPixels, index))
            .collect(Collectors.toList());
    }

    private boolean hasEnoughDistance(List<OfficePosition> allPositions, List<Integer> state, double minimumDistanceInPixels, Integer index) {

        Set<Integer> indicesInStateInTheSameGroup = groups.stream()
            .filter(group -> group.contains(index))
            .flatMap(List::stream)
            .filter(state::contains)
            .collect(Collectors.toSet());

        int stateSize = state.size();
        int indiSize = indicesInStateInTheSameGroup.size();

        return indicesInStateInTheSameGroup
            .stream()
            .allMatch(
                stateIndex -> allPositions.get(stateIndex).distanceFrom(allPositions.get(index)) > minimumDistanceInPixels
            );
    }



//    public List<OfficePosition> getOptimalPositionDistribution(
//        double minimumDistanceInPixels,
//        List<OfficePosition> allPositions
//    ) {
//        int count = allPositions.size();
//        int bookableCount = allPositions.size();
//
//        allPositions.sort(Comparator.comparingInt(OfficePosition::getX));
//        int minX = allPositions.get(0).getX();
//        int maxX = allPositions.get(count - 1).getX();
//
//        allPositions.sort(Comparator.comparingInt(OfficePosition::getY));
//        int minY = allPositions.get(0).getY();
//        int maxY = allPositions.get(count - 1).getY();
//
//        int distance = (int)minimumDistanceInPixels;
//
////        List<List<OfficePosition>> groups = new ArrayList<>();
//
//        IntStream.range(minX - distance, maxX + distance).filter(x -> x % minimumDistanceInPixels == 0).forEach(
//            x -> {
//                IntStream.range(minY - distance, maxY + distance).filter(y -> y % minimumDistanceInPixels == 0).forEach(
//                    y -> {
//                        groups.add(putPositionsToGroup(allPositions, x, y, distance));
//                    }
//                );
//            }
//        );
//
//        System.out.println(groups);
//
//
//
//        while (bookableCount > 0) {
//            System.out.println("try bookable count:" + bookableCount);
//            Optional<List<OfficePosition>> validDistribution = getValidDistribution(allPositions, bookableCount, minimumDistanceInPixels);
//            if (validDistribution.isPresent()) {
//                return validDistribution.get();
//            }
//            bookableCount = bookableCount - 1;
//        }
//
//        return null;
//    }

    private List<Integer> putPositionsToGroup(List<OfficePosition> allPositions, int x, int y, int delta) {

        int minX = x - delta;
        int maxX = x + delta;
        int minY = y - delta;
        int maxY = y + delta;

        System.out.println("TO GROUP: X: " + minX + "-" + maxX + "   Y: " + minY + "-" + maxY);

        List<Integer> group = new ArrayList<>();

        IntStream.range(0, allPositions.size()).forEach(
            index -> {
                OfficePosition pos = allPositions.get(index);
                if (pos.getX() > minX && pos.getX() < maxX && pos.getY() > minY && pos.getY() < maxY) {
                    group.add(index);
                }
            }
        );

        return group;
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
