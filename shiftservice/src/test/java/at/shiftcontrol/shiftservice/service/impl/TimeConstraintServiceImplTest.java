package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.TimeConstraintDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.TimeConstraintCreateDto;
import at.shiftcontrol.shiftservice.dto.TimeConstraintDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentId;
import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.entity.TimeConstraint;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.type.TimeConstraintType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeConstraintServiceImplTest {

    @Mock
    private TimeConstraintDao timeConstraintDao;
    @Mock
    private VolunteerDao volunteerDao;
    @Mock
    private EventDao eventDao;
    @Mock
    private AssignmentDao assignmentDao;

    @InjectMocks
    private TimeConstraintServiceImpl service;

    private static final String USER_ID = "vol-1";
    private static final long EVENT_ID = 42L;

    // Helper
    private Volunteer mockVolunteerWithEvent() {
        Event event = new Event();
        event.setId(EVENT_ID);
        ShiftPlan plan = new ShiftPlan();
        plan.setEvent(event);
        Volunteer v = new Volunteer();
        v.setId(USER_ID);
        v.setVolunteeringPlans(Set.of(plan));
        return v;
    }

    @Test
    void createTimeConstraint_volunteerMissing_throwsConflictException() {
        var dto = TimeConstraintCreateDto.builder()
            .type(TimeConstraintType.UNAVAILABLE)
            .from(Instant.parse("2030-01-01T09:00:00Z"))
            .to(Instant.parse("2030-01-01T11:00:00Z"))
            .build();

        when(eventDao.findById(EVENT_ID)).thenReturn(Optional.of(new Event()));
        when(volunteerDao.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createTimeConstraint(dto, USER_ID, EVENT_ID))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("Volunteer not found");
    }

    @Test
    void createTimeConstraint_volunteerNotPartOfEvent_throwsConflict() {
        var dto = TimeConstraintCreateDto.builder()
            .type(TimeConstraintType.UNAVAILABLE)
            .from(Instant.parse("2030-01-01T09:00:00Z"))
            .to(Instant.parse("2030-01-01T11:00:00Z"))
            .build();

        when(eventDao.findById(EVENT_ID)).thenReturn(Optional.of(new Event()));
        Volunteer v = new Volunteer(); // no volunteering plans
        when(volunteerDao.findById(USER_ID)).thenReturn(Optional.of(v));

        assertThatThrownBy(() -> service.createTimeConstraint(dto, USER_ID, EVENT_ID))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("not part of event");
    }

    @Test
    void createTimeConstraint_overlapExisting_unavailable_throwsConflict() {
        var dto = TimeConstraintCreateDto.builder()
            .type(TimeConstraintType.UNAVAILABLE)
            .from(Instant.parse("2030-01-01T09:00:00Z"))
            .to(Instant.parse("2030-01-01T12:00:00Z"))
            .build();

        when(eventDao.findById(EVENT_ID)).thenReturn(Optional.of(new Event()));
        when(volunteerDao.findById(USER_ID)).thenReturn(Optional.of(mockVolunteerWithEvent()));

        TimeConstraint existing = new TimeConstraint();
        existing.setStartTime(Instant.parse("2030-01-01T10:00:00Z"));
        existing.setEndTime(Instant.parse("2030-01-01T13:00:00Z"));
        existing.setId(99L);

        when(timeConstraintDao.searchByVolunteerAndEvent(USER_ID, EVENT_ID)).thenReturn(List.of(existing));

        assertThatThrownBy(() -> service.createTimeConstraint(dto, USER_ID, EVENT_ID))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("overlaps with existing");
    }

    @Test
    void createTimeConstraint_assignmentOverlap_throwsConflict() {
        var dto = TimeConstraintCreateDto.builder()
            .type(TimeConstraintType.UNAVAILABLE)
            .from(Instant.parse("2030-01-01T09:00:00Z"))
            .to(Instant.parse("2030-01-01T11:00:00Z"))
            .build();

        when(eventDao.findById(EVENT_ID)).thenReturn(Optional.of(new Event()));
        when(volunteerDao.findById(USER_ID)).thenReturn(Optional.of(mockVolunteerWithEvent()));
        when(timeConstraintDao.searchByVolunteerAndEvent(USER_ID, EVENT_ID)).thenReturn(List.of());

        Assignment a = new Assignment();
        a.setId(AssignmentId.of(1L, USER_ID));
        when(assignmentDao.getConflictingAssignments(eq(USER_ID), any(), any()))
            .thenReturn(List.of(a));

        assertThatThrownBy(() -> service.createTimeConstraint(dto, USER_ID, EVENT_ID))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("overlaps with existing assignments");
    }

    @Test
    void createTimeConstraint_emergency_invalidWholeDays_throwsBadRequest() {
        var dto = TimeConstraintCreateDto.builder()
            .type(TimeConstraintType.EMERGENCY)
            .from(Instant.parse("2030-01-01T10:00:00Z"))
            .to(Instant.parse("2030-01-02T00:00:00Z"))
            .build();

        when(eventDao.findById(EVENT_ID)).thenReturn(Optional.of(new Event()));
        when(volunteerDao.findById(USER_ID)).thenReturn(Optional.of(mockVolunteerWithEvent()));

        assertThatThrownBy(() -> service.createTimeConstraint(dto, USER_ID, EVENT_ID))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("whole days");
    }

    @Test
    void delete_missingConstraint_throwsNotFound() {
        when(timeConstraintDao.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(123L))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("not found");
    }

    @Test
    void delete_existing_deletesSuccessfully() throws NotFoundException, ForbiddenException {
        TimeConstraint tc = new TimeConstraint();
        tc.setId(321L);

        when(timeConstraintDao.findById(321L)).thenReturn(Optional.of(tc));

        service.delete(321L);
        verify(timeConstraintDao).delete(tc);
    }

    @Test
    void createTimeConstraint_success_unavailable_returnsDto() throws ConflictException, ForbiddenException {
        var dto = TimeConstraintCreateDto.builder()
            .type(TimeConstraintType.UNAVAILABLE)
            .from(Instant.parse("2030-01-01T09:00:00Z"))
            .to(Instant.parse("2030-01-01T11:00:00Z"))
            .build();

        when(eventDao.findById(EVENT_ID)).thenReturn(Optional.of(new Event()));
        when(volunteerDao.findById(USER_ID)).thenReturn(Optional.of(mockVolunteerWithEvent()));
        when(timeConstraintDao.searchByVolunteerAndEvent(USER_ID, EVENT_ID)).thenReturn(List.of());
        when(assignmentDao.getConflictingAssignments(eq(USER_ID), any(), any())).thenReturn(List.of());

        TimeConstraint saved = new TimeConstraint();
        saved.setId(10L);
        saved.setStartTime(dto.getFrom());
        saved.setEndTime(dto.getTo());

        when(timeConstraintDao.save(any(TimeConstraint.class))).thenReturn(saved);

        TimeConstraintDto result = service.createTimeConstraint(dto, USER_ID, EVENT_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("10");
        assertThat(result.getFrom()).isEqualTo(dto.getFrom());
        assertThat(result.getTo()).isEqualTo(dto.getTo());
    }

}
