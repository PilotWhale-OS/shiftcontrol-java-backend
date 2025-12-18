package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.ShiftDao;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.repo.ShiftRepository;

@RequiredArgsConstructor
@Component
public class ShiftDaoImpl implements ShiftDao {
    private final ShiftRepository shiftRepository;

    @Override
    public Optional<Shift> findById(Long id) {
        return shiftRepository.findById(id);
    }

    @Override
    public Shift save(Shift entity) {
        return shiftRepository.save(entity);
    }

    @Override
    public Collection<Shift> saveAll(Collection<Shift> entities) {
        return shiftRepository.saveAll(entities);
    }

    @Override
    public void delete(Shift entity) {
        shiftRepository.delete(entity);
    }
}
