package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.shiftservice.dto.leaderboard.LeaderBoardDto;

public interface LeaderboardService {
    LeaderBoardDto getLeaderBoard(long eventId);
}
