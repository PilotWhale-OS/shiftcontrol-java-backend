package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import jakarta.ws.rs.NotFoundException;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.shiftservice.dto.leaderboard.LeaderBoardDto;

public interface LeaderboardService {
    LeaderBoardDto getLeaderBoard(long eventId) throws NotFoundException, ForbiddenException;
}
