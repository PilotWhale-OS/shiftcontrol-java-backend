package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.util.List;

import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.auth.user.AssignedUser;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.RewardPointsTransactionDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.OverallStatisticsDto;
import at.shiftcontrol.shiftservice.dto.OwnStatisticsDto;
import at.shiftcontrol.shiftservice.dto.event.EventSearchDto;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.service.StatisticService;
import at.shiftcontrol.shiftservice.service.impl.event.EventServiceImpl;
import at.shiftcontrol.shiftservice.util.SecurityHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {
    @Mock
    private EventDao eventDao;

    @Mock
    private VolunteerDao volunteerDao;

    @Mock
    private StatisticService statisticService;

    @Mock
    private ApplicationUserProvider userProvider;

    @Mock
    private RewardPointsTransactionDao rewardPointsTransactionDao;

    @Mock
    private SecurityHelper securityHelper;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void search_filtersEventsByVolunteerShiftPlans() {
        // Arrange: create shift plans
        ShiftPlan spRelevant = new ShiftPlan();
        spRelevant.setId(1L);
        ShiftPlan spIrrelevant = new ShiftPlan();
        spIrrelevant.setId(2L);

        // Arrange: event that matches (contains spRelevant)
        Event relevantEvent = new Event();
        relevantEvent.setShiftPlans(List.of(spRelevant));
        relevantEvent.setStartTime(Instant.MIN);
        relevantEvent.setEndTime(Instant.MAX);

        // Arrange: event that does NOT match (contains spIrrelevant)
        Event nonRelevantEvent = new Event();
        nonRelevantEvent.setShiftPlans(List.of(spIrrelevant));

        var assignedUser = mock(AssignedUser.class);
        when(assignedUser.getUserId()).thenReturn("420696742");
        when(userProvider.getCurrentUser()).thenReturn(assignedUser);

        // DAO returns both events
        when(eventDao.search(any(EventSearchDto.class)))
            .thenReturn(List.of(relevantEvent, nonRelevantEvent));

        // Volunteer participates in spRelevant only
        Volunteer volunteer = mock(Volunteer.class);
        when(volunteer.getVolunteeringPlans()).thenReturn(List.of(spRelevant));
        when(volunteerDao.getById("420696742")).thenReturn(volunteer);

        // Expected result is only the relevant event mapped
        var expected = EventMapper.toEventDto(List.of(relevantEvent));

        // Act
        var result = eventService.search(new EventSearchDto());

        // Assert
        verify(eventDao).search(any(EventSearchDto.class));
        verify(volunteerDao).getById("420696742");
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getUserRelatedShiftPlansOfEvent_throwsWhenEventNotFound() {
        long eventId = 999L;
        String userId = "420696742";

        when(eventDao.getById(eventId)).thenThrow(NotFoundException.class);
        ;

        assertThatThrownBy(() -> eventService.getUserRelatedShiftPlansOfEvent(eventId, userId))
            .isInstanceOf(NotFoundException.class);

        verify(eventDao).getById(eventId);
        verifyNoInteractions(statisticService);
    }

    @Test
    void getUserRelatedShiftPlansOfEvent_throwsWhenVolunteerNotFound() {
        long eventId = 1L;
        String userId = "missing";

        Event event = new Event();
        event.setShiftPlans(List.of());

        when(volunteerDao.getById(userId)).thenThrow(NotFoundException.class);

        assertThatThrownBy(() -> eventService.getUserRelatedShiftPlansOfEvent(eventId, userId))
            .isInstanceOf(NotFoundException.class);

        verify(volunteerDao).getById(userId);
        verifyNoInteractions(statisticService, eventDao);
    }

    @Test
    void getEventShiftPlansOverview_buildsOverviewFromEventShiftPlansAndStatistics() {
        long eventId = 1L;
        String userId = "420696742";

        // ShiftPlans (again: different IDs)
        ShiftPlan spRelevant = new ShiftPlan();
        spRelevant.setId(1L);
        ShiftPlan spIrrelevant = new ShiftPlan();
        spIrrelevant.setId(2L);

        Event event = new Event();
        event.setShiftPlans(List.of(spRelevant, spIrrelevant));
        event.setStartTime(Instant.MIN);
        event.setEndTime(Instant.MIN);

        Volunteer volunteer = mock(Volunteer.class);
        when(volunteer.getVolunteeringPlans()).thenReturn(List.of(spRelevant));

        when(eventDao.getById(eventId)).thenReturn(event);
        when(volunteerDao.getById(userId)).thenReturn(volunteer);

        // Stats (use your real DTO types here)
        OwnStatisticsDto ownStats = mock(OwnStatisticsDto.class);
        OverallStatisticsDto overallStats = mock(OverallStatisticsDto.class);

        when(statisticService.getOwnStatisticsOfShiftPlans(List.of(spRelevant), userId)).thenReturn(ownStats);
        when(statisticService.getOverallEventStatistics(event)).thenReturn(overallStats);
        when(rewardPointsTransactionDao.sumPointsByVolunteerAndEvent(userId, eventId)).thenReturn(10L);

        var result = eventService.getEventShiftPlansOverview(eventId, userId);

        // expected pieces
        var expectedEventOverview = EventMapper.toEventDto(event);
        var expectedShiftPlans = ShiftPlanMapper.toShiftPlanDto(List.of(spRelevant));

        assertThat(result.getEventOverview()).isEqualTo(expectedEventOverview);
        assertThat(result.getShiftPlans()).isEqualTo(expectedShiftPlans);
        assertThat(result.getRewardPoints()).isEqualTo(10L);
        assertThat(result.getOwnEventStatistics()).isEqualTo(ownStats);
        assertThat(result.getOverallEventStatistics()).isEqualTo(overallStats);

        verify(eventDao, times(2)).getById(eventId); // called twice since shiftPlans is fetched via call to getUserRelatedShiftPlansOfEvent
        verify(volunteerDao).getById(userId);
        verify(statisticService).getOwnStatisticsOfShiftPlans(List.of(spRelevant), userId);
        verify(statisticService).getOverallEventStatistics(event);
    }

    @Test
    void getEventShiftPlansOverview_throwsWhenEventNotFound() {
        long eventId = 999L;
        String userId = "420696742";

        when(eventDao.getById(eventId)).thenThrow(NotFoundException.class);
        ;

        assertThatThrownBy(() -> eventService.getEventShiftPlansOverview(eventId, userId))
            .isInstanceOf(NotFoundException.class);

        verify(eventDao).getById(eventId);
        verifyNoInteractions(volunteerDao, statisticService);
    }
}
