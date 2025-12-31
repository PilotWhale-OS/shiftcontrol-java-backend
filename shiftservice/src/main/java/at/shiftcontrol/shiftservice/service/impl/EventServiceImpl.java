package at.shiftcontrol.shiftservice.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.event.EventDto;
import at.shiftcontrol.shiftservice.dto.event.EventModificationDto;
import at.shiftcontrol.shiftservice.dto.event.EventSearchDto;
import at.shiftcontrol.shiftservice.dto.event.EventShiftPlansOverviewDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDto;
import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.service.EventService;
import at.shiftcontrol.shiftservice.service.StatisticService;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventDao eventDao;
    private final VolunteerDao volunteerDao;
    private final StatisticService statisticService;
    private final EventService eventService;

    @Override
    public List<EventDto> search(EventSearchDto searchDto, String userId) throws NotFoundException {
        var filteredEvents = eventDao.search(searchDto);
        var volunteer = volunteerDao.findById(userId).orElseThrow(() -> new NotFoundException("Volunteer not found with id: " + userId));
        var volunteerShiftPlans = volunteer.getVolunteeringPlans();

        // filter events that the volunteer is part of
        var relevantEvents = filteredEvents.stream()
            .filter(event -> event.getShiftPlans().stream()
                .anyMatch(volunteerShiftPlans::contains))
            .toList();

        return EventMapper.toEventDto(relevantEvents);
    }

    @Override
    public List<ShiftPlanDto> getUserRelatedShiftPlansOfEvent(long eventId, String userId) throws NotFoundException {
        return ShiftPlanMapper.toShiftPlanDto(getUserRelatedShiftPlanEntitiesOfEvent(eventId, userId));
    }

    @Override
    public EventShiftPlansOverviewDto getEventShiftPlansOverview(long eventId, String userId) throws NotFoundException {
        var event = eventDao.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));

        var eventOverviewDto = EventMapper.toEventDto(event);
        var userRelevantShiftPlans = getUserRelatedShiftPlanEntitiesOfEvent(eventId, userId);

        //Todo: implement reward points
        return EventShiftPlansOverviewDto.builder()
            .eventOverview(eventOverviewDto)
            .shiftPlans(ShiftPlanMapper.toShiftPlanDto(userRelevantShiftPlans))
            .rewardPoints(-1)
            .ownEventStatistics(statisticService.getOwnStatisticsOfShiftPlans(userRelevantShiftPlans, userId))
            .overallEventStatistics(statisticService.getOverallEventStatistics(event))
            .build();
    }

    @Override
    public EventDto createEvent(EventModificationDto modificationDto) {
        Event event = EventMapper.toEvent(modificationDto);
        return EventMapper.toEventDto(event);
    }

    @Override
    public EventDto updateEvent(long eventId, EventModificationDto eventModificationDto) throws NotFoundException { // todo check if belongs
        Event event = eventDao.findById(eventId).orElseThrow(NotFoundException::new);
        EventMapper.updateEvent(event, eventModificationDto);
        eventDao.save(event);
        return  EventMapper.toEventDto(event);
    }

    @Override
    public void deleteEvent(long eventId) throws NotFoundException {
        eventDao.delete(eventDao.findById(eventId).orElseThrow(NotFoundException::new));
    }

    private List<ShiftPlan> getUserRelatedShiftPlanEntitiesOfEvent(long eventId, String userId) throws NotFoundException {
        var event = eventDao.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));
        var shiftPlans = event.getShiftPlans();
        var volunteer = volunteerDao.findById(userId).orElseThrow(() -> new NotFoundException("Volunteer not found with id: " + userId));

        var volunteerShiftPlans = volunteer.getVolunteeringPlans();

        // filter shiftPlans that the volunteer is part of (volunteerShiftPlans)
        return shiftPlans.stream()
            .filter(volunteerShiftPlans::contains)
            .toList();
    }
}
