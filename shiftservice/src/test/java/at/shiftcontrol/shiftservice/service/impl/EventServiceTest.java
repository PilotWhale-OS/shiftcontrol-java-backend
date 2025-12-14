package at.shiftcontrol.shiftservice.service.impl;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dto.EventSearchDto;
import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {
    @Mock
    private EventDao eventDao;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void searchEvents() {
        var events = List.of(mock(Event.class));
        var eventsDto = EventMapper.toEventOverviewDto(events);
        when(eventDao.search(any())).thenReturn(events);

        var result = eventService.search(new EventSearchDto());

        Mockito.verify(eventDao).search(any());
        assertThat(result).isEqualTo(eventsDto);
    }
}
