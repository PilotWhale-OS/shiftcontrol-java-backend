package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.leaderboard.LeaderBoardDto;

public interface LeaderboardService {
    Collection<LeaderBoardDto> getLeaderBoard(long eventId);
}
