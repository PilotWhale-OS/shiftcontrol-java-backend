package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dto.event.EventSearchDto;
import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.repo.EventRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EventDaoImpl implements EventDao {
    private final EventRepository eventRepository;

    @Override
    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    @Override
    public Event save(Event entity) {
        return eventRepository.save(entity);
    }

    @Override
    public Collection<Event> saveAll(Collection<Event> entities) {
        return eventRepository.saveAll(entities);
    }

    @Override
    public void delete(Event entity) {
        eventRepository.delete(entity);
    }

    @Override
    public List<Event> search(EventSearchDto searchDto) {
        return eventRepository.findAll((Specification<Event>) (root, query, criteriaBuilder) -> {
            if (searchDto == null || StringUtils.isBlank(searchDto.getName())) {
                return null; //Select all
            }
            var predicates = criteriaBuilder.conjunction();

            if (searchDto.getName() != null && !searchDto.getName().isEmpty()) {
                var nameLower = searchDto.getName().toLowerCase().trim();
                predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + nameLower + "%"));
            }

            return predicates;
        });
    }
}
