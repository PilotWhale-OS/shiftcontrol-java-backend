package at.shiftcontrol.shiftservice.service.impl.event;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.AssignmentId;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequest;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequestId;
import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.AssignmentEvent;
import at.shiftcontrol.lib.event.events.AssignmentSwitchEvent;
import at.shiftcontrol.lib.event.events.EventEvent;
import at.shiftcontrol.lib.event.events.PositionSlotVolunteerEvent;
import at.shiftcontrol.lib.event.events.TradeEvent;
import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.shiftservice.annotation.AdminOnly;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dao.ActivityDao;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dao.AssignmentSwitchRequestDao;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.event.EventDto;
import at.shiftcontrol.shiftservice.dto.event.EventModificationDto;
import at.shiftcontrol.shiftservice.dto.event.EventScheduleDaySearchDto;
import at.shiftcontrol.shiftservice.dto.event.EventScheduleDto;
import at.shiftcontrol.shiftservice.dto.event.EventSearchDto;
import at.shiftcontrol.shiftservice.dto.event.EventShiftPlansOverviewDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDto;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.service.StatisticService;
import at.shiftcontrol.shiftservice.service.event.EventService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventDao eventDao;
    private final ActivityDao activityDao;
    private final VolunteerDao volunteerDao;
    private final StatisticService statisticService;
    private final ApplicationUserProvider userProvider;
    private final SecurityHelper securityHelper;
    private final ApplicationEventPublisher publisher;

    // TODO delete unused fields
    private final AssignmentDao assignmentDao;
    private final AssignmentSwitchRequestDao assignmentSwitchRequestDao;;


    @Override
    public EventDto getEvent(long eventId) {
        var event = eventDao.getById(eventId);
        securityHelper.assertUserIsAllowedToAccessEvent(event);

        return EventMapper.toEventDto(event);
    }

    @Override
    public List<EventDto> search(EventSearchDto searchDto) {
        var filteredEvents = eventDao.search(searchDto);
        var currentUser = userProvider.getCurrentUser();

        // skip filtering for admin users
        if (securityHelper.isUserAdmin(currentUser)) {
            return EventMapper.toEventDto(filteredEvents);
        }
        String userId = currentUser.getUserId();

        var volunteer = volunteerDao.getById(userId);
        var volunteerShiftPlans = volunteer.getVolunteeringPlans();
        var planningShiftPlans = volunteer.getPlanningPlans();

        // filter events that the volunteer is part of
        var relevantEvents = filteredEvents.stream()
            .filter(event -> event.getShiftPlans().stream()
                .anyMatch(shiftPlan -> volunteerShiftPlans.contains(shiftPlan) || planningShiftPlans.contains(shiftPlan))
            )
            .toList();

        return EventMapper.toEventDto(relevantEvents);
    }

    @Override
    public List<ShiftPlanDto> getUserRelatedShiftPlansOfEvent(long eventId, String userId) {
        return ShiftPlanMapper.toShiftPlanDto(getUserRelatedShiftPlanEntitiesOfEvent(eventId, userId));
    }

    @Override
    public EventShiftPlansOverviewDto getEventShiftPlansOverview(long eventId, String userId) {
        var event = eventDao.getById(eventId);

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
    public EventScheduleDto getEventSchedule(long eventId, EventScheduleDaySearchDto searchDto) {
        var event = eventDao.getById(eventId);
        securityHelper.assertUserIsAllowedToAccessEvent(event);

        var activitiesOfEvent = activityDao.searchActivitiesInEvent(eventId, searchDto).stream().toList();

        return EventMapper.toEventScheduleDto(event, activitiesOfEvent);
    }

    @Override
    @AdminOnly
    public EventDto createEvent(@NonNull EventModificationDto modificationDto) {
        validateEventModificationDto(modificationDto);

        Event event = EventMapper.toEvent(modificationDto);
        event = eventDao.save(event);

        publisher.publishEvent(EventEvent.of(RoutingKeys.EVENT_CREATED, event));
        return EventMapper.toEventDto(event);
    }

    @Override
    @AdminOnly
    public EventDto updateEvent(long eventId, @NonNull EventModificationDto modificationDto) {
        validateEventModificationDto(modificationDto);

        Event event = eventDao.getById(eventId);
        EventMapper.updateEvent(event, modificationDto);
        eventDao.save(event);

        publisher.publishEvent(EventEvent.of(RoutingKeys.format(RoutingKeys.EVENT_UPDATED, Map.of("eventId", String.valueOf(eventId))), event));
        return EventMapper.toEventDto(event);
    }

    private void validateEventModificationDto(EventModificationDto modificationDto) {
        if (modificationDto.getStartTime().isAfter(modificationDto.getEndTime())) {
            throw new BadRequestException("Event end time must be after start time");
        }
    }

    @Override
    @AdminOnly
    public void deleteEvent(long eventId) {
        var event = eventDao.getById(eventId);

        publisher.publishEvent(EventEvent.of(RoutingKeys.format(RoutingKeys.EVENT_DELETED, Map.of("eventId", String.valueOf(eventId))), event));
        eventDao.delete(event);
    }

    // TODO delete this test method
    @Override
    public boolean sendTestEvent(String testEvent) {

        long positionSlotId = 1L;
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";

        long positionSlotIdRequested = 2L;
        String userIdRequested = "28c02050-4f90-4f3a-b1df-3c7d27a166e6";

        Assignment assignment = assignmentDao.getById(AssignmentId.of(positionSlotId, userId));
        AssignmentSwitchRequest trade = assignmentSwitchRequestDao.getById(
            AssignmentSwitchRequestId.of(
                AssignmentId.of(positionSlotId, userId),
                AssignmentId.of(positionSlotIdRequested, userIdRequested)
            )
        );

        switch (testEvent) {
            case "POSITIONSLOT_JOINED":
                // - POSITIONSLOT_JOINED
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_JOINED,
                        Map.of("positionSlotId", String.valueOf(assignment.getPositionSlot().getId()),
                            "volunteerId", assignment.getAssignedVolunteer().getId())),
                    assignment.getPositionSlot(), assignment.getAssignedVolunteer().getId()));
                break;
            case "POSITIONSLOT_LEFT":
                // - POSITIONSLOT_LEFT
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_LEFT,
                        Map.of("positionSlotId", String.valueOf(assignment.getPositionSlot().getId()),
                            "volunteerId", assignment.getAssignedVolunteer().getId())),
                    assignment.getPositionSlot(), assignment.getAssignedVolunteer().getId()));
                break;
            case "TRADE_REQUEST_CREATED":
                // - TRADE_REQUEST_CREATED
                publisher.publishEvent(TradeEvent.of(RoutingKeys.format(RoutingKeys.TRADE_REQUEST_CREATED,
                    Map.of("requestedVolunteerId", trade.getRequestedAssignment().getAssignedVolunteer().getId(),
                        "offeringVolunteerId", trade.getOfferingAssignment().getAssignedVolunteer().getId())), trade
                ));
                break;
            case "TRADE_REQUEST_DECLINED":
                // - TRADE_REQUEST_DECLINED
                publisher.publishEvent(TradeEvent.of(RoutingKeys.format(RoutingKeys.TRADE_REQUEST_DECLINED,
                    Map.of("requestedVolunteerId", trade.getRequestedAssignment().getAssignedVolunteer().getId(),
                        "offeringVolunteerId", trade.getOfferingAssignment().getAssignedVolunteer().getId())), trade
                ));
                break;
            case "TRADE_REQUEST_CANCELED":
                // - TRADE_REQUEST_CANCELED
                publisher.publishEvent(TradeEvent.of(RoutingKeys.format(RoutingKeys.TRADE_REQUEST_CANCELED,
                    Map.of("requestedVolunteerId", trade.getRequestedAssignment().getAssignedVolunteer().getId(),
                        "offeringVolunteerId", trade.getOfferingAssignment().getAssignedVolunteer().getId())), trade
                ));
                break;
            case "TRADE_REQUEST_COMPLETED":
                // - TRADE_REQUEST_COMPLETED
                publisher.publishEvent(AssignmentSwitchEvent.of(
                    trade.getRequestedAssignment(), trade.getOfferingAssignment()));
                break;
            case "AUCTION_CREATED":
                // - AUCTION_CREATED
                publisher.publishEvent(AssignmentEvent.of(
                    RoutingKeys.format(RoutingKeys.AUCTION_CREATED,
                        Map.of("positionSlotId", String.valueOf(positionSlotId))
                    ), assignment
                ));
                break;
            case "AUCTION_CLAIMED":
                // - AUCTION_CLAIMED
                publisher.publishEvent(AssignmentEvent.of(RoutingKeys.format(RoutingKeys.AUCTION_CLAIMED, Map.of(
                    "positionSlotId", String.valueOf(assignment.getPositionSlot().getId()),
                    "oldVolunteerId", userIdRequested)), assignment
                ));
                break;
            case "AUCTION_CANCELED":
                // - AUCTION_CANCELED
                publisher.publishEvent(AssignmentEvent.of(
                    RoutingKeys.format(
                        RoutingKeys.AUCTION_CANCELED,
                        Map.of("positionSlotId", String.valueOf(positionSlotId))
                    ), assignment
                ));
                break;
            case "POSITIONSLOT_REQUEST_LEAVE":
                // - POSITIONSLOT_REQUEST_LEAVE
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_LEAVE,
                        Map.of("positionSlotId", String.valueOf(positionSlotId),
                            "volunteerId", userId)),
                    assignment.getPositionSlot(), userId));
                break;
            case "POSITIONSLOT_REQUEST_LEAVE_ACCEPTED":
                // - POSITIONSLOT_REQUEST_LEAVE_ACCEPTED
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_LEAVE_ACCEPTED,
                        Map.of("positionSlotId", String.valueOf(positionSlotId),
                            "volunteerId", userId)),
                    assignment.getPositionSlot(), userId));
                break;
            case "POSITIONSLOT_REQUEST_LEAVE_DECLINED":
                // - POSITIONSLOT_REQUEST_LEAVE_DECLINED
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_LEAVE_DECLINED,
                        Map.of("positionSlotId", String.valueOf(positionSlotId),
                            "volunteerId", userId)),
                    assignment.getPositionSlot(), userId));
                break;
            default:
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_JOINED,
                        Map.of("positionSlotId", String.valueOf(assignment.getPositionSlot().getId()),
                            "volunteerId", assignment.getAssignedVolunteer().getId())),
                    assignment.getPositionSlot(), assignment.getAssignedVolunteer().getId()));
                // - POSITIONSLOT_LEFT
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_LEFT,
                        Map.of("positionSlotId", String.valueOf(assignment.getPositionSlot().getId()),
                            "volunteerId", assignment.getAssignedVolunteer().getId())),
                    assignment.getPositionSlot(), assignment.getAssignedVolunteer().getId()));
                // - TRADE_REQUEST_CREATED
                publisher.publishEvent(TradeEvent.of(RoutingKeys.format(RoutingKeys.TRADE_REQUEST_CREATED,
                    Map.of("requestedVolunteerId", trade.getRequestedAssignment().getAssignedVolunteer().getId(),
                        "offeringVolunteerId", trade.getOfferingAssignment().getAssignedVolunteer().getId())), trade
                ));
                // - TRADE_REQUEST_DECLINED
                publisher.publishEvent(TradeEvent.of(RoutingKeys.format(RoutingKeys.TRADE_REQUEST_DECLINED,
                    Map.of("requestedVolunteerId", trade.getRequestedAssignment().getAssignedVolunteer().getId(),
                        "offeringVolunteerId", trade.getOfferingAssignment().getAssignedVolunteer().getId())), trade
                ));
                // - TRADE_REQUEST_CANCELED
                publisher.publishEvent(TradeEvent.of(RoutingKeys.format(RoutingKeys.TRADE_REQUEST_CANCELED,
                    Map.of("requestedVolunteerId", trade.getRequestedAssignment().getAssignedVolunteer().getId(),
                        "offeringVolunteerId", trade.getOfferingAssignment().getAssignedVolunteer().getId())), trade
                ));
                // - TRADE_REQUEST_COMPLETED
                publisher.publishEvent(AssignmentSwitchEvent.of(
                    trade.getRequestedAssignment(), trade.getOfferingAssignment()));
                // - AUCTION_CREATED
                publisher.publishEvent(AssignmentEvent.of(
                    RoutingKeys.format(RoutingKeys.AUCTION_CREATED,
                        Map.of("positionSlotId", String.valueOf(positionSlotId))
                    ), assignment
                ));
                // - AUCTION_CLAIMED
                publisher.publishEvent(AssignmentEvent.of(RoutingKeys.format(RoutingKeys.AUCTION_CLAIMED, Map.of(
                    "positionSlotId", String.valueOf(assignment.getPositionSlot().getId()),
                    "oldVolunteerId", userIdRequested)), assignment
                ));
                // - AUCTION_CANCELED
                publisher.publishEvent(AssignmentEvent.of(
                    RoutingKeys.format(
                        RoutingKeys.AUCTION_CANCELED,
                        Map.of("positionSlotId", String.valueOf(positionSlotId))
                    ), assignment
                ));
                // - POSITIONSLOT_REQUEST_LEAVE
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_LEAVE,
                        Map.of("positionSlotId", String.valueOf(positionSlotId),
                            "volunteerId", userId)),
                    assignment.getPositionSlot(), userId));
                // - POSITIONSLOT_REQUEST_LEAVE_ACCEPTED
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_LEAVE_ACCEPTED,
                        Map.of("positionSlotId", String.valueOf(positionSlotId),
                            "volunteerId", userId)),
                    assignment.getPositionSlot(), userId));
                // - POSITIONSLOT_REQUEST_LEAVE_DECLINED
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_LEAVE_DECLINED,
                        Map.of("positionSlotId", String.valueOf(positionSlotId),
                            "volunteerId", userId)),
                    assignment.getPositionSlot(), userId));
        }

        return true;
    }

    private List<ShiftPlan> getUserRelatedShiftPlanEntitiesOfEvent(long eventId, String userId) {
        var event = eventDao.getById(eventId);
        var shiftPlans = event.getShiftPlans();

        // skip filtering for admin users
        if (securityHelper.isUserAdmin()) {
            return shiftPlans.stream().toList();
        }

        var volunteer = volunteerDao.getById(userId);

        var volunteerShiftPlans = volunteer.getVolunteeringPlans();
        var planningShiftPlans = volunteer.getPlanningPlans();

        // filter shiftPlans that the volunteer is part of (volunteerShiftPlans)
        return shiftPlans.stream()
            .filter(shiftPlan -> volunteerShiftPlans.contains(shiftPlan) || planningShiftPlans.contains(shiftPlan))
            .toList();
    }
}
