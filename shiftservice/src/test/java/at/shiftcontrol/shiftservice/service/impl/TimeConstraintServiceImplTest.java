package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.shiftservice.dao.AttendanceDao;
import at.shiftcontrol.shiftservice.dao.AttendanceTimeConstraintDao;
import at.shiftcontrol.shiftservice.dto.TimeConstraintCreateDto;
import at.shiftcontrol.shiftservice.dto.TimeConstraintDto;
import at.shiftcontrol.shiftservice.entity.Attendance;
import at.shiftcontrol.shiftservice.entity.AttendanceId;
import at.shiftcontrol.shiftservice.entity.AttendanceTimeConstraint;
import at.shiftcontrol.shiftservice.type.TimeConstraintType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeConstraintServiceImplTest {
    @Mock
    private AttendanceTimeConstraintDao attendanceTimeConstraintDao;

    @Mock
    private AttendanceDao attendanceDao;

    @InjectMocks
    private TimeConstraintServiceImpl timeConstraintService;

    private static final String USER_ID = "vol-1";
    private static final long EVENT_ID = 42L;

    @Test
    void createTimeConstraint_invalidRange_throwsConflictException() {
        var dto = TimeConstraintCreateDto.builder()
            .type(TimeConstraintType.UNAVAILABLE)
            .from(Instant.parse("2025-01-01T10:00:00Z"))
            .to(Instant.parse("2025-01-01T10:00:00Z")) // equal -> invalid (not before)
            .build();

        assertThatThrownBy(() -> timeConstraintService.createTimeConstraint(dto, USER_ID, EVENT_ID))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("Invalid time range");
    }

    @Test
    void createTimeConstraint_attendanceMissing_throwsConflictException() {
        var dto = TimeConstraintCreateDto.builder()
            .type(TimeConstraintType.UNAVAILABLE)
            .from(Instant.parse("2025-01-01T09:00:00Z"))
            .to(Instant.parse("2025-01-01T11:00:00Z"))
            .build();

        when(attendanceDao.findById(any(AttendanceId.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeConstraintService.createTimeConstraint(dto, USER_ID, EVENT_ID))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("Cannot create time constraint because attendance");
    }

    @Test
    void createTimeConstraint_overlapsWithExisting_throwsConflictException() {
        // existing constraint: [10:00,20:00]
        AttendanceTimeConstraint existing = AttendanceTimeConstraint.builder()
            .id(1L)
            .startTime(Instant.parse("2025-01-01T10:00:00Z"))
            .endTime(Instant.parse("2025-01-01T20:00:00Z"))
            .build();

        when(attendanceTimeConstraintDao.searchByVolunteerAndEvent(USER_ID, EVENT_ID))
            .thenReturn(List.of(existing));

        when(attendanceDao.findById(any(AttendanceId.class))).thenReturn(Optional.of(new Attendance()));

        // overlapping: inside existing
        var dto = TimeConstraintCreateDto.builder()
            .type(TimeConstraintType.UNAVAILABLE)
            .from(Instant.parse("2025-01-01T12:00:00Z"))
            .to(Instant.parse("2025-01-01T13:00:00Z"))
            .build();

        assertThatThrownBy(() -> timeConstraintService.createTimeConstraint(dto, USER_ID, EVENT_ID))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("overlaps");
    }

    @Test
    void createTimeConstraint_touchingExisting_atEnd_succeeds() throws ConflictException {
        // existing constraint: [10:00,20:00]
        AttendanceTimeConstraint existing = AttendanceTimeConstraint.builder()
            .id(3L)
            .startTime(Instant.parse("2025-01-01T10:00:00Z"))
            .endTime(Instant.parse("2025-01-01T20:00:00Z"))
            .build();

        when(attendanceTimeConstraintDao.searchByVolunteerAndEvent(USER_ID, EVENT_ID))
            .thenReturn(List.of(existing));

        Attendance attendance = Attendance.builder().id(AttendanceId.of(USER_ID, EVENT_ID)).build();
        when(attendanceDao.findById(any(AttendanceId.class))).thenReturn(Optional.of(attendance));

        // touching before: to == existing.start
        var dto = TimeConstraintCreateDto.builder()
            .type(TimeConstraintType.UNAVAILABLE)
            .from(Instant.parse("2025-01-01T05:00:00Z"))
            .to(Instant.parse("2025-01-01T10:00:00Z"))
            .build();

        // simulate save returning entity with id
        AttendanceTimeConstraint saved = AttendanceTimeConstraint.builder()
            .id(99L)
            .attendance(attendance)
            .type(TimeConstraintType.UNAVAILABLE)
            .startTime(dto.getFrom())
            .endTime(dto.getTo())
            .build();

        when(attendanceTimeConstraintDao.save(any(AttendanceTimeConstraint.class))).thenReturn(saved);

        TimeConstraintDto result = timeConstraintService.createTimeConstraint(dto, USER_ID, EVENT_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(String.valueOf(saved.getId()));
        assertThat(result.getFrom()).isEqualTo(dto.getFrom());
        assertThat(result.getTo()).isEqualTo(dto.getTo());
    }

    @Test
    void createTimeConstraint_touchingExisting_atStart_succeeds() throws ConflictException {
        // existing constraint: [10:00,20:00]
        AttendanceTimeConstraint existing = AttendanceTimeConstraint.builder()
            .id(3L)
            .startTime(Instant.parse("2025-01-01T10:00:00Z"))
            .endTime(Instant.parse("2025-01-01T20:00:00Z"))
            .build();

        when(attendanceTimeConstraintDao.searchByVolunteerAndEvent(USER_ID, EVENT_ID))
            .thenReturn(List.of(existing));

        Attendance attendance = Attendance.builder().id(AttendanceId.of(USER_ID, EVENT_ID)).build();
        when(attendanceDao.findById(any(AttendanceId.class))).thenReturn(Optional.of(attendance));

        // touching before: to == existing.start
        var dto = TimeConstraintCreateDto.builder()
            .type(TimeConstraintType.UNAVAILABLE)
            .from(Instant.parse("2025-01-01T20:00:00Z"))
            .to(Instant.parse("2025-01-01T21:00:00Z"))
            .build();

        // simulate save returning entity with id
        AttendanceTimeConstraint saved = AttendanceTimeConstraint.builder()
            .id(99L)
            .attendance(attendance)
            .type(TimeConstraintType.UNAVAILABLE)
            .startTime(dto.getFrom())
            .endTime(dto.getTo())
            .build();

        when(attendanceTimeConstraintDao.save(any(AttendanceTimeConstraint.class))).thenReturn(saved);

        TimeConstraintDto result = timeConstraintService.createTimeConstraint(dto, USER_ID, EVENT_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(String.valueOf(saved.getId()));
        assertThat(result.getFrom()).isEqualTo(dto.getFrom());
        assertThat(result.getTo()).isEqualTo(dto.getTo());
    }

    @Test
    void createTimeConstraint_nonOverlappingBefore_succeeds() throws ConflictException {
        // existing constraint: [10:00,20:00]
        AttendanceTimeConstraint existing = AttendanceTimeConstraint.builder()
            .id(3L)
            .startTime(Instant.parse("2025-01-01T10:00:00Z"))
            .endTime(Instant.parse("2025-01-01T20:00:00Z"))
            .build();

        when(attendanceTimeConstraintDao.searchByVolunteerAndEvent(USER_ID, EVENT_ID))
            .thenReturn(List.of(existing));

        Attendance attendance = Attendance.builder().id(AttendanceId.of(USER_ID, EVENT_ID)).build();
        when(attendanceDao.findById(any(AttendanceId.class))).thenReturn(Optional.of(attendance));

        // non-overlapping before: to < existing.start
        var dto = TimeConstraintCreateDto.builder()
            .type(TimeConstraintType.UNAVAILABLE)
            .from(Instant.parse("2025-01-01T05:00:00Z"))
            .to(Instant.parse("2025-01-01T09:00:00Z"))
            .build();

        // simulate save returning entity with id
        AttendanceTimeConstraint saved = AttendanceTimeConstraint.builder()
            .id(99L)
            .attendance(attendance)
            .type(TimeConstraintType.UNAVAILABLE)
            .startTime(dto.getFrom())
            .endTime(dto.getTo())
            .build();

        when(attendanceTimeConstraintDao.save(any(AttendanceTimeConstraint.class))).thenReturn(saved);

        TimeConstraintDto result = timeConstraintService.createTimeConstraint(dto, USER_ID, EVENT_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(String.valueOf(saved.getId()));
        assertThat(result.getFrom()).isEqualTo(dto.getFrom());
        assertThat(result.getTo()).isEqualTo(dto.getTo());
    }

    @Test
    void createTimeConstraint_nonOverlappingAfter_succeeds() throws ConflictException {
        // existing constraint: [10:00,20:00]
        AttendanceTimeConstraint existing = AttendanceTimeConstraint.builder()
            .id(4L)
            .startTime(Instant.parse("2025-01-01T10:00:00Z"))
            .endTime(Instant.parse("2025-01-01T20:00:00Z"))
            .build();

        when(attendanceTimeConstraintDao.searchByVolunteerAndEvent(USER_ID, EVENT_ID))
            .thenReturn(List.of(existing));

        Attendance attendance = Attendance.builder().id(AttendanceId.of(USER_ID, EVENT_ID)).build();
        when(attendanceDao.findById(any(AttendanceId.class))).thenReturn(Optional.of(attendance));

        // non-overlapping after: from > existing.end
        var dto = TimeConstraintCreateDto.builder()
            .type(TimeConstraintType.UNAVAILABLE)
            .from(Instant.parse("2025-01-01T21:00:00Z"))
            .to(Instant.parse("2025-01-01T22:00:00Z"))
            .build();

        AttendanceTimeConstraint saved = AttendanceTimeConstraint.builder()
            .id(100L)
            .attendance(attendance)
            .type(TimeConstraintType.UNAVAILABLE)
            .startTime(dto.getFrom())
            .endTime(dto.getTo())
            .build();

        when(attendanceTimeConstraintDao.save(any(AttendanceTimeConstraint.class))).thenReturn(saved);

        TimeConstraintDto result = timeConstraintService.createTimeConstraint(dto, USER_ID, EVENT_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(String.valueOf(saved.getId()));
        assertThat(result.getFrom()).isEqualTo(dto.getFrom());
        assertThat(result.getTo()).isEqualTo(dto.getTo());
    }

    @Test
    void createTimeConstraint_emergency_notWholeDays_throwsBadRequest() {
        var dto = TimeConstraintCreateDto.builder()
            .type(TimeConstraintType.EMERGENCY)
            .from(Instant.parse("2025-01-01T10:00:00Z"))
            .to(Instant.parse("2025-01-02T00:00:00Z"))
            .build();

        Attendance attendance = Attendance.builder().id(AttendanceId.of(USER_ID, EVENT_ID)).build();
        when(attendanceDao.findById(any(AttendanceId.class))).thenReturn(Optional.of(attendance));

        assertThatThrownBy(() -> timeConstraintService.createTimeConstraint(dto, USER_ID, EVENT_ID))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("whole days");
    }

    @Test
    void createTimeConstraint_emergency_wholeDays_queriesByType_and_succeeds() throws ConflictException {
        var dto = TimeConstraintCreateDto.builder()
            .type(TimeConstraintType.EMERGENCY)
            .from(Instant.parse("2025-01-01T00:00:00Z"))
            .to(Instant.parse("2025-01-03T00:00:00Z"))
            .build();

        Attendance attendance = Attendance.builder().id(AttendanceId.of(USER_ID, EVENT_ID)).build();
        when(attendanceDao.findById(any(AttendanceId.class))).thenReturn(Optional.of(attendance));

        when(attendanceTimeConstraintDao.searchByVolunteerAndEventAndType(USER_ID, EVENT_ID, TimeConstraintType.EMERGENCY))
            .thenReturn(List.of());

        AttendanceTimeConstraint saved = AttendanceTimeConstraint.builder()
            .id(101L)
            .attendance(attendance)
            .type(TimeConstraintType.EMERGENCY)
            .startTime(dto.getFrom())
            .endTime(dto.getTo())
            .build();

        when(attendanceTimeConstraintDao.save(any(AttendanceTimeConstraint.class))).thenReturn(saved);

        TimeConstraintDto result = timeConstraintService.createTimeConstraint(dto, USER_ID, EVENT_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(String.valueOf(saved.getId()));
        assertThat(result.getFrom()).isEqualTo(dto.getFrom());
        assertThat(result.getTo()).isEqualTo(dto.getTo());
    }

    @Test
    void createTimeConstraint_emergency_overlapWithExistingTypeSpecific_throwsConflict() {
        AttendanceTimeConstraint existing = AttendanceTimeConstraint.builder()
            .id(55L)
            .type(TimeConstraintType.EMERGENCY)
            .startTime(Instant.parse("2025-01-02T00:00:00Z"))
            .endTime(Instant.parse("2025-01-04T00:00:00Z"))
            .build();

        var dto = TimeConstraintCreateDto.builder()
            .type(TimeConstraintType.EMERGENCY)
            .from(Instant.parse("2025-01-03T00:00:00Z"))
            .to(Instant.parse("2025-01-05T00:00:00Z"))
            .build();

        Attendance attendance = Attendance.builder().id(AttendanceId.of(USER_ID, EVENT_ID)).build();
        when(attendanceDao.findById(any(AttendanceId.class))).thenReturn(Optional.of(attendance));

        when(attendanceTimeConstraintDao.searchByVolunteerAndEventAndType(USER_ID, EVENT_ID, TimeConstraintType.EMERGENCY))
            .thenReturn(List.of(existing));

        assertThatThrownBy(() -> timeConstraintService.createTimeConstraint(dto, USER_ID, EVENT_ID))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("overlaps");
    }

}
