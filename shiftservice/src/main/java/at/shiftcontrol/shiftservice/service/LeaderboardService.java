package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import jakarta.ws.rs.NotFoundException;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.shiftservice.dto.leaderboard.LeaderBoardDto;

import lombok.NonNull;

public interface LeaderboardService {
    @NonNull LeaderBoardDto getLeaderBoard(long eventId, @NonNull String currentUserId) throws NotFoundException, ForbiddenException;
}
