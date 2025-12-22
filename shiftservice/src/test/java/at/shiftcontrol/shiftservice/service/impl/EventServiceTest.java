package at.shiftcontrol.shiftservice.service.impl;

import java.util.List;
import java.util.Optional;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.EventSearchDto;
import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {
    @Mock
    private EventDao eventDao;

    @Mock
    private VolunteerDao volunteerDao;

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
}
