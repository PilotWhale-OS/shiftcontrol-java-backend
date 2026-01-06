package at.shiftcontrol.shiftservice.service.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.auth.KeycloakUserService;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dto.leaderboard.LeaderBoardDto;
import at.shiftcontrol.shiftservice.dto.leaderboard.RankDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.service.LeaderboardService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;

@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {
    private static final int LEADERBOARD_LIMIT = 10;

    private final EventDao eventDao;
    private final SecurityHelper securityHelper;
    private final KeycloakUserService keycloakService;

    @Override
    public LeaderBoardDto getLeaderBoard(long eventId) {
        var event = eventDao.getById(eventId);
        securityHelper.assertUserIsAllowedToAccessEvent(event);

        List<Assignment> allAssignments = ensureList(event.getShiftPlans()).stream()
            .flatMap(plan -> ensureList(plan.getShifts()).stream())
            .flatMap(shift -> ensureList(shift.getSlots()).stream())
            .flatMap(slot -> ensureList(slot.getAssignments()).stream())
            .toList();

        Map<String, Long> minutesByVolunteer = new HashMap<>();

        for (Assignment a : allAssignments) {
            Shift shift = a.getPositionSlot().getShift();

            long minutes = Duration.between(shift.getStartTime(), shift.getEndTime()).toMinutes();
            String volunteerId = a.getAssignedVolunteer().getId();
            minutesByVolunteer.merge(volunteerId, minutes, Long::sum);
        }

        List<Map.Entry<String, Long>> topVolunteers =
            minutesByVolunteer.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(LEADERBOARD_LIMIT)
                .toList();

        List<RankDto> ranks = new ArrayList<>(topVolunteers.size());
        int rank = 1;

        for (var entry : topVolunteers) {
            String volunteerId = entry.getKey();
            long totalMinutes = entry.getValue();

            var user = keycloakService.getUserById(volunteerId);

            ranks.add(RankDto.builder()
                .rank(rank++)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .hours((int) (totalMinutes / 60.0)) // or keep minutes
                .build());
        }
        return LeaderBoardDto.builder()
            .size(ranks.size())
            .ranks(ranks)
            .build();
    }

    private static <T> Collection<T> ensureList(Collection<T> c) {
        return c == null ? List.of() : c;
    }
}
