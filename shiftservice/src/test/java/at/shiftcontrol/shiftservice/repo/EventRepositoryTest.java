package at.shiftcontrol.shiftservice.repo;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import config.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.shiftservice.dto.rows.PlanVolunteerIdRow;

@DataJpaTest
@Import({TestConfig.class})
public class EventRepositoryTest {
    @Autowired
    private EventRepository eventRepository;

    @Test
    void testGetAllEvents() {
        List<Event> events = eventRepository.findAll();
        Assertions.assertFalse(events.isEmpty());
    }

    @Test
    void testGetPlannersForEventAndUser() {
        Collection<PlanVolunteerIdRow> rows = eventRepository
            .getPlannersForEventAndUser(3L, "28c02050-4f90-4f3a-b1df-3c7d27a166e5");
        Assertions.assertFalse(rows.isEmpty());
    }
}
