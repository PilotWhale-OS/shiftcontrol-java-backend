package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Optional;

import org.springframework.stereotype.Component;

import at.shiftcontrol.shiftservice.entity.PositionSlot;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.repo.PositionSlotRepository;

@RequiredArgsConstructor
@Component
public class PositionSlotDaoImpl implements PositionSlotDao {
    private final PositionSlotRepository positionSlotRepository;

    @Override
    public Optional<PositionSlot> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public PositionSlot save(PositionSlot entity) {
        return null;
    }

    @Override
    public void delete(PositionSlot entity) {
    }
}
