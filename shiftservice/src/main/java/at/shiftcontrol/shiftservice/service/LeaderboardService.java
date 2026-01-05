package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.leaderboard.LeaderBoardDto;

public interface LeaderboardService {
    Collection<LeaderBoardDto> getLeaderBoard(long eventId) throws NotFoundException, ForbiddenException;
}
