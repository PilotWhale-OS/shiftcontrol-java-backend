package at.shiftcontrol.shiftservice.service.impl.event;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequest;
import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.AssignmentEvent;
import at.shiftcontrol.lib.event.events.AssignmentSwitchEvent;
import at.shiftcontrol.lib.event.events.EventEvent;
import at.shiftcontrol.lib.event.events.PositionSlotVolunteerEvent;
import at.shiftcontrol.lib.event.events.TradeEvent;
import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.shiftservice.annotation.AdminOnly;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.auth.KeycloakUserService;
import at.shiftcontrol.shiftservice.dao.ActivityDao;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dao.AssignmentSwitchRequestDao;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.RewardPointsTransactionDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.event.EventDto;
import at.shiftcontrol.shiftservice.dto.event.EventModificationDto;
import at.shiftcontrol.shiftservice.dto.event.EventSearchDto;
import at.shiftcontrol.shiftservice.dto.event.EventShiftPlansOverviewDto;
import at.shiftcontrol.shiftservice.dto.rows.PlanVolunteerIdRow;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanContactInfoDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDto;
import at.shiftcontrol.shiftservice.dto.user.ContactInfoDto;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.mapper.UserAssemblingMapper;
import at.shiftcontrol.shiftservice.service.StatisticService;
import at.shiftcontrol.shiftservice.service.event.EventService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventDao eventDao;
    private final ActivityDao activityDao;
    private final VolunteerDao volunteerDao;
    private final RewardPointsTransactionDao rewardPointsTransactionDao;
    private final StatisticService statisticService;
    private final ApplicationUserProvider userProvider;
    private final SecurityHelper securityHelper;
    private final ApplicationEventPublisher publisher;
    private final KeycloakUserService keycloakUserService;

    // TODO delete unused fields
    private final AssignmentDao assignmentDao;
    private final AssignmentSwitchRequestDao assignmentSwitchRequestDao;


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
    public Collection<ShiftPlanContactInfoDto> getPlannerContactInfo(long eventId, String userId) {
        // check access
        var event = eventDao.getById(eventId);
        securityHelper.assertUserIsAllowedToAccessEvent(event);

        // get planner IDs of users plans
        Collection<PlanVolunteerIdRow> rows = eventDao.getPlannersForEventAndUser(eventId, userId);

        // get all planners from keycloak
        Set<String> allPlannerIds =
            rows.stream()
                .map(PlanVolunteerIdRow::getVolunteerId)
                .collect(Collectors.toSet());
        Collection<UserRepresentation> planners = keycloakUserService.getUserByIds(allPlannerIds);

        // create maps to lookup userIds per plan, planNames and planner-userRepresentations
        Map<Long, List<String>> volunteerIdsByPlan =
            rows.stream()
                .collect(Collectors.groupingBy(
                    PlanVolunteerIdRow::getPlanId,
                    LinkedHashMap::new,
                    Collectors.mapping(
                        PlanVolunteerIdRow::getVolunteerId,
                        Collectors.toList()
                    )
                ));
        Map<Long, String> planNames =
            rows.stream()
                .collect(Collectors.toMap(
                    PlanVolunteerIdRow::getPlanId,
                    PlanVolunteerIdRow::getPlanName,
                    (a, b) -> a
                ));
        Map<String, ContactInfoDto> contactInfoById =
            planners.stream()
                .collect(Collectors.toMap(
                    UserRepresentation::getId,
                    UserAssemblingMapper::toContactInfoDto
                ));

        // create result
        return volunteerIdsByPlan.entrySet().stream()
                .map(entry -> {
                    Long planId = entry.getKey();
                    String planName = planNames.get(planId);
                    List<ContactInfoDto> contacts =
                        entry.getValue().stream()
                            .map(contactInfoById::get)
                            .toList();

                    return new ShiftPlanContactInfoDto(String.valueOf(planId), planName, contacts);
                })
                .toList();
    }

    @Override
    public EventShiftPlansOverviewDto getEventShiftPlansOverview(long eventId, String userId) {
        var event = eventDao.getById(eventId);

        var eventOverviewDto = EventMapper.toEventDto(event);
        var userRelevantShiftPlans = getUserRelatedShiftPlanEntitiesOfEvent(eventId, userId);

        return EventShiftPlansOverviewDto.builder()
            .eventOverview(eventOverviewDto)
            .shiftPlans(ShiftPlanMapper.toShiftPlanDto(userRelevantShiftPlans))
            .rewardPoints((int) rewardPointsTransactionDao.sumPointsByVolunteerAndEvent(userId, eventId))
            .ownEventStatistics(statisticService.getOwnStatisticsOfShiftPlans(userRelevantShiftPlans, userId))
            .overallEventStatistics(statisticService.getOverallEventStatistics(event))
            .build();
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

        var eventEvent = EventEvent.of(RoutingKeys.format(RoutingKeys.EVENT_DELETED, Map.of("eventId", String.valueOf(eventId))), event);
        publisher.publishEvent(eventEvent);
        eventDao.delete(event);
    }

    // TODO delete this test method
    private static long testIdFix = 0;
    private static long testIdIncrementing = 0;

    @Override
    public boolean sendTestEvent(String testEvent) {
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        String userIdRequested = "28c02050-4f90-4f3a-b1df-3c7d27a166e6";

        Assignment assignmentFix = getAssignment(userId, testIdFix);
        Assignment assignmentInc = getAssignment(userId, ++testIdIncrementing);
        AssignmentSwitchRequest trade = getAssignmentSwitchRequest(userId, ++testIdIncrementing, userIdRequested, ++testIdIncrementing);

        switch (testEvent) {
            case "POSITIONSLOT_JOINED":
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_JOINED,
                        Map.of("positionSlotId", String.valueOf(assignmentFix.getPositionSlot().getId()),
                            "volunteerId", assignmentFix.getAssignedVolunteer().getId())),
                    assignmentFix.getPositionSlot(), assignmentFix.getAssignedVolunteer().getId()));
                break;
            case "POSITIONSLOT_LEFT":
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_LEFT,
                        Map.of("positionSlotId", String.valueOf(assignmentFix.getPositionSlot().getId()),
                            "volunteerId", assignmentFix.getAssignedVolunteer().getId())),
                    assignmentFix.getPositionSlot(), assignmentFix.getAssignedVolunteer().getId()));
                break;
            case "TRADE_REQUEST_CREATED":
                publisher.publishEvent(TradeEvent.of(RoutingKeys.format(RoutingKeys.TRADE_REQUEST_CREATED,
                    Map.of("requestedVolunteerId", trade.getRequestedAssignment().getAssignedVolunteer().getId(),
                        "offeringVolunteerId", trade.getOfferingAssignment().getAssignedVolunteer().getId())), trade
                ));
                break;
            case "TRADE_REQUEST_DECLINED":
                publisher.publishEvent(TradeEvent.of(RoutingKeys.format(RoutingKeys.TRADE_REQUEST_DECLINED,
                    Map.of("requestedVolunteerId", trade.getRequestedAssignment().getAssignedVolunteer().getId(),
                        "offeringVolunteerId", trade.getOfferingAssignment().getAssignedVolunteer().getId())), trade
                ));
                break;
            case "TRADE_REQUEST_CANCELED":
                publisher.publishEvent(TradeEvent.of(RoutingKeys.format(RoutingKeys.TRADE_REQUEST_CANCELED,
                    Map.of("requestedVolunteerId", trade.getRequestedAssignment().getAssignedVolunteer().getId(),
                        "offeringVolunteerId", trade.getOfferingAssignment().getAssignedVolunteer().getId())), trade
                ));
                break;
            case "TRADE_REQUEST_COMPLETED":
                publisher.publishEvent(AssignmentSwitchEvent.of(
                    trade.getRequestedAssignment(), trade.getOfferingAssignment()));
                break;
            case "AUCTION_CREATED":
                publisher.publishEvent(AssignmentEvent.of(
                    RoutingKeys.format(RoutingKeys.AUCTION_CREATED,
                        Map.of("positionSlotId", String.valueOf(assignmentInc.getPositionSlot().getId()))
                    ), assignmentInc
                ));
                break;
            case "AUCTION_CLAIMED":
                publisher.publishEvent(AssignmentEvent.of(RoutingKeys.format(RoutingKeys.AUCTION_CLAIMED, Map.of(
                    "positionSlotId", String.valueOf(assignmentInc.getPositionSlot().getId()),
                    "oldVolunteerId", userIdRequested)), assignmentInc
                ));
                break;
            case "AUCTION_CANCELED":
                publisher.publishEvent(AssignmentEvent.of(
                    RoutingKeys.format(
                        RoutingKeys.AUCTION_CANCELED,
                        Map.of("positionSlotId", String.valueOf(assignmentInc.getPositionSlot().getId()))
                    ), assignmentInc
                ));
                break;
            case "POSITIONSLOT_REQUEST_LEAVE":
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_LEAVE,
                        Map.of("positionSlotId", String.valueOf(assignmentFix.getPositionSlot().getId()),
                            "volunteerId", userId)),
                    assignmentFix.getPositionSlot(), userId));
                break;
            case "POSITIONSLOT_REQUEST_JOIN":
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_JOIN,
                        Map.of("positionSlotId", String.valueOf(assignmentFix.getPositionSlot().getId()),
                            "volunteerId", userId)),
                    assignmentFix.getPositionSlot(), userId));
                break;
            case "POSITIONSLOT_REQUEST_LEAVE_ACCEPTED":
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_LEAVE_ACCEPTED,
                        Map.of("positionSlotId", String.valueOf(assignmentInc.getPositionSlot().getId()),
                            "volunteerId", userId)),
                    assignmentInc.getPositionSlot(), userId));
                break;
            case "POSITIONSLOT_REQUEST_LEAVE_DECLINED":
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_LEAVE_DECLINED,
                        Map.of("positionSlotId", String.valueOf(assignmentInc.getPositionSlot().getId()),
                            "volunteerId", userId)),
                    assignmentInc.getPositionSlot(), userId));
                break;
            case "POSITIONSLOT_REQUEST_JOIN_WITHDRAW":
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_JOIN_WITHDRAW,
                        Map.of("positionSlotId", String.valueOf(assignmentFix.getPositionSlot().getId()),
                            "volunteerId", userId)),
                    assignmentFix.getPositionSlot(), userId));
                break;
            case "POSITIONSLOT_REQUEST_LEAVE_WITHDRAW":
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_LEAVE_WITHDRAW,
                        Map.of("positionSlotId", String.valueOf(assignmentFix.getPositionSlot().getId()),
                            "volunteerId", userId)),
                    assignmentFix.getPositionSlot(), userId));
                break;
            default:
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_JOINED,
                        Map.of("positionSlotId", String.valueOf(assignmentInc.getPositionSlot().getId()),
                            "volunteerId", assignmentInc.getAssignedVolunteer().getId())),
                    assignmentInc.getPositionSlot(), assignmentInc.getAssignedVolunteer().getId()));
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_LEFT,
                        Map.of("positionSlotId", String.valueOf(assignmentInc.getPositionSlot().getId()),
                            "volunteerId", assignmentInc.getAssignedVolunteer().getId())),
                    assignmentInc.getPositionSlot(), assignmentInc.getAssignedVolunteer().getId()));
                publisher.publishEvent(TradeEvent.of(RoutingKeys.format(RoutingKeys.TRADE_REQUEST_CREATED,
                    Map.of("requestedVolunteerId", trade.getRequestedAssignment().getAssignedVolunteer().getId(),
                        "offeringVolunteerId", trade.getOfferingAssignment().getAssignedVolunteer().getId())), trade
                ));
                publisher.publishEvent(TradeEvent.of(RoutingKeys.format(RoutingKeys.TRADE_REQUEST_DECLINED,
                    Map.of("requestedVolunteerId", trade.getRequestedAssignment().getAssignedVolunteer().getId(),
                        "offeringVolunteerId", trade.getOfferingAssignment().getAssignedVolunteer().getId())), trade
                ));
                publisher.publishEvent(TradeEvent.of(RoutingKeys.format(RoutingKeys.TRADE_REQUEST_CANCELED,
                    Map.of("requestedVolunteerId", trade.getRequestedAssignment().getAssignedVolunteer().getId(),
                        "offeringVolunteerId", trade.getOfferingAssignment().getAssignedVolunteer().getId())), trade
                ));
                publisher.publishEvent(AssignmentSwitchEvent.of(
                    trade.getRequestedAssignment(), trade.getOfferingAssignment()));
                publisher.publishEvent(AssignmentEvent.of(
                    RoutingKeys.format(RoutingKeys.AUCTION_CREATED,
                        Map.of("positionSlotId", String.valueOf(assignmentInc.getPositionSlot().getId()))
                    ), assignmentInc
                ));
                publisher.publishEvent(AssignmentEvent.of(RoutingKeys.format(RoutingKeys.AUCTION_CLAIMED, Map.of(
                    "positionSlotId", String.valueOf(assignmentInc.getPositionSlot().getId()),
                    "oldVolunteerId", userIdRequested)), assignmentInc
                ));
                publisher.publishEvent(AssignmentEvent.of(
                    RoutingKeys.format(
                        RoutingKeys.AUCTION_CANCELED,
                        Map.of("positionSlotId", String.valueOf(assignmentInc.getPositionSlot().getId()))
                    ), assignmentInc
                ));
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_LEAVE,
                        Map.of("positionSlotId", String.valueOf(assignmentInc.getPositionSlot().getId()),
                            "volunteerId", userId)),
                    assignmentInc.getPositionSlot(), userId));
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_JOIN,
                        Map.of("positionSlotId", String.valueOf(assignmentFix.getPositionSlot().getId()),
                            "volunteerId", userId)),
                    assignmentFix.getPositionSlot(), userId));
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_LEAVE_ACCEPTED,
                        Map.of("positionSlotId", String.valueOf(assignmentInc.getPositionSlot().getId()),
                            "volunteerId", userId)),
                    assignmentInc.getPositionSlot(), userId));
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_LEAVE_DECLINED,
                        Map.of("positionSlotId", String.valueOf(assignmentInc.getPositionSlot().getId()),
                            "volunteerId", userId)),
                    assignmentInc.getPositionSlot(), userId));
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_JOIN_WITHDRAW,
                        Map.of("positionSlotId", String.valueOf(assignmentFix.getPositionSlot().getId()),
                            "volunteerId", userId)),
                    assignmentFix.getPositionSlot(), userId));
                publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_LEAVE_WITHDRAW,
                        Map.of("positionSlotId", String.valueOf(assignmentFix.getPositionSlot().getId()),
                            "volunteerId", userId)),
                    assignmentFix.getPositionSlot(), userId));
        }

        return true;
    }

    private static AssignmentSwitchRequest getAssignmentSwitchRequest(String offeringUserId, long offeringSlotId,
                                                                      String requestingUserId, long requestingSlotId) {
        Assignment offering = getAssignment(offeringUserId, offeringSlotId);
        Assignment requesting = getAssignment(requestingUserId, requestingSlotId);
        return AssignmentSwitchRequest.builder()
            .offeringAssignment(offering)
            .requestedAssignment(requesting)
            .build();
    }

    private static Assignment getAssignment(String volunteerId, long slotId) {
        return Assignment.builder()
            .assignedVolunteer(getVolunteer(volunteerId))
            .positionSlot(getPositionSlot(slotId))
            .build();
    }

    private static Volunteer getVolunteer(String id) {
        return Volunteer.builder()
            .id(id)
            .build();
    }

    private static PositionSlot getPositionSlot(long id) {
        return PositionSlot.builder()
            .id(id)
            .build();
    }

    // TODO delete ABOVE

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
