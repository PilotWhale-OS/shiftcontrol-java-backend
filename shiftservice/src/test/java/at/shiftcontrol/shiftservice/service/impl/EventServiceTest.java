package at.shiftcontrol.shiftservice.service.impl;

import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dto.EventSearchDto;
import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.service.EventService;

import config.TestConfig;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import({TestConfig.class})
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
