package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.repo.PositionSlotRepository;

@RequiredArgsConstructor
@Component
public class PositionSlotDaoImpl implements PositionSlotDao {
    private final PositionSlotRepository positionSlotRepository;

    @Override
    public Optional<PositionSlot> findById(Long id) {
        return positionSlotRepository.findById(id);
    }

    @Override
    public PositionSlot save(PositionSlot entity) {
        return positionSlotRepository.save(entity);
    }

    @Override
    public Collection<PositionSlot> saveAll(Collection<PositionSlot> entities) {
        return positionSlotRepository.saveAll(entities);
    }

    @Override
    public void delete(PositionSlot entity) {
        positionSlotRepository.delete(entity);
    }
}
