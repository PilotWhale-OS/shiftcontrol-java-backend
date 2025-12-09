package at.shiftcontrol.shiftservice.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dto.EventDto;
import at.shiftcontrol.shiftservice.dto.EventSearchDto;
import at.shiftcontrol.shiftservice.dto.EventShiftPlansOverviewDto;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.service.EventService;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventDao eventDao;

    @Override
    public List<EventDto> search(EventSearchDto searchDto) {
        return EventMapper.toEventOverviewDto(eventDao.search(searchDto));
    }

    @Override
    public EventShiftPlansOverviewDto getEventShiftPlansOverview(long eventId, long userId) throws NotFoundException {
        var event = eventDao.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));

        //Todo: implement reward points and statistics
        return new EventShiftPlansOverviewDto(EventMapper.toEventOverviewDto(event),
            null,
            null,
            -1,
            ShiftPlanMapper.shiftPlansToShiftPlanDtos(event.getShiftPlans()));
    }
}
