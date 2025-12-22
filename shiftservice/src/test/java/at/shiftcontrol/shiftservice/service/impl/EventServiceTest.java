package at.shiftcontrol.shiftservice.service.impl;

import java.util.List;
import java.util.Optional;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.EventSearchDto;
import at.shiftcontrol.shiftservice.dto.OverallStatisticsDto;
import at.shiftcontrol.shiftservice.dto.OwnStatisticsDto;
import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.service.StatisticService;
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
public class EventServiceTest {
    @Mock
    private EventDao eventDao;

    @Mock
    private VolunteerDao volunteerDao;

    @Mock
    private StatisticService statisticService;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void search_filtersEventsByVolunteerShiftPlans() throws NotFoundException {
        // Arrange: create shift plans
        ShiftPlan spRelevant = new ShiftPlan();
        spRelevant.setId(1L);
        ShiftPlan spIrrelevant = new ShiftPlan();
        spIrrelevant.setId(2L);

        // Arrange: event that matches (contains spRelevant)
        Event relevantEvent = new Event();
        relevantEvent.setShiftPlans(List.of(spRelevant));

        // Arrange: event that does NOT match (contains spIrrelevant)
        Event nonRelevantEvent = new Event();
        nonRelevantEvent.setShiftPlans(List.of(spIrrelevant));

        // DAO returns both events
        when(eventDao.search(any(EventSearchDto.class)))
            .thenReturn(List.of(relevantEvent, nonRelevantEvent));

        // Volunteer participates in spRelevant only
        Volunteer volunteer = mock(Volunteer.class);
        when(volunteer.getVolunteeringPlans()).thenReturn(List.of(spRelevant));
        when(volunteerDao.findById("420696742")).thenReturn(Optional.of(volunteer));

        // Expected result is only the relevant event mapped
        var expected = EventMapper.toEventOverviewDto(List.of(relevantEvent));

        // Act
        var result = eventService.search(new EventSearchDto(), "420696742");

        // Assert
        verify(eventDao).search(any(EventSearchDto.class));
        verify(volunteerDao).findById("420696742");
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getUserRelatedShiftPlansOfEvent_throwsWhenEventNotFound() {
        long eventId = 999L;
        String userId = "420696742";

        when(eventDao.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getUserRelatedShiftPlansOfEvent(eventId, userId))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Event not found with id: " + eventId);

        verify(eventDao).findById(eventId);
        verifyNoInteractions(volunteerDao, statisticService);
    }

    @Test
    void getUserRelatedShiftPlansOfEvent_throwsWhenVolunteerNotFound() {
        long eventId = 1L;
        String userId = "missing";

        Event event = new Event();
        event.setShiftPlans(List.of());

        when(eventDao.findById(eventId)).thenReturn(Optional.of(event));
        when(volunteerDao.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getUserRelatedShiftPlansOfEvent(eventId, userId))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Volunteer not found with id: " + userId);

        verify(eventDao).findById(eventId);
        verify(volunteerDao).findById(userId);
        verifyNoInteractions(statisticService);
    }

    @Test
    void getEventShiftPlansOverview_buildsOverviewFromEventShiftPlansAndStatistics() throws NotFoundException {
        long eventId = 1L;
        String userId = "420696742";

        // ShiftPlans (again: different IDs)
        ShiftPlan spRelevant = new ShiftPlan();
        spRelevant.setId(1L);
        ShiftPlan spIrrelevant = new ShiftPlan();
        spIrrelevant.setId(2L);

        Event event = new Event();
        event.setShiftPlans(List.of(spRelevant, spIrrelevant));

        Volunteer volunteer = mock(Volunteer.class);
        when(volunteer.getVolunteeringPlans()).thenReturn(List.of(spRelevant));

        when(eventDao.findById(eventId)).thenReturn(Optional.of(event));
        when(volunteerDao.findById(userId)).thenReturn(Optional.of(volunteer));

        // Stats (use your real DTO types here)
        OwnStatisticsDto ownStats = mock(OwnStatisticsDto.class);
        OverallStatisticsDto overallStats = mock(OverallStatisticsDto.class);

        when(statisticService.getOwnEventStatistics(eventId, userId)).thenReturn(ownStats);
        when(statisticService.getOverallEventStatistics(eventId)).thenReturn(overallStats);

        var result = eventService.getEventShiftPlansOverview(eventId, userId);

        // expected pieces
        var expectedEventOverview = EventMapper.toEventOverviewDto(event);
        var expectedShiftPlans = ShiftPlanMapper.toShiftPlanDto(List.of(spRelevant));

        assertThat(result.getEventOverview()).isEqualTo(expectedEventOverview);
        assertThat(result.getShiftPlans()).isEqualTo(expectedShiftPlans);
        assertThat(result.getRewardPoints()).isEqualTo(-1);
        assertThat(result.getOwnEventStatistics()).isEqualTo(ownStats);
        assertThat(result.getOverallEventStatistics()).isEqualTo(overallStats);

        verify(eventDao, times(2)).findById(eventId); // called twice since shiftPlans is fetched via call to getUserRelatedShiftPlansOfEvent
        verify(volunteerDao).findById(userId);
        verify(statisticService).getOwnEventStatistics(eventId, userId);
        verify(statisticService).getOverallEventStatistics(eventId);
    }

    @Test
    void getEventShiftPlansOverview_throwsWhenEventNotFound() {
        long eventId = 999L;
        String userId = "420696742";

        when(eventDao.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventShiftPlansOverview(eventId, userId))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Event not found with id: " + eventId);

        verify(eventDao).findById(eventId);
        verifyNoInteractions(volunteerDao, statisticService);
    }
}
