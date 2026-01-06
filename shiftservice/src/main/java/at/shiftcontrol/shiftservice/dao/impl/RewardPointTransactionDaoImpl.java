package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.Optional;

import at.shiftcontrol.shiftservice.dao.RewardPointTransactionDao;
import at.shiftcontrol.shiftservice.dto.rewardpoints.EventPointsDto;
import at.shiftcontrol.shiftservice.entity.RewardPointTransaction;
import at.shiftcontrol.shiftservice.mapper.RewardPointsMapper;
import at.shiftcontrol.shiftservice.repo.RewardPointTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RewardPointTransactionDaoImpl implements RewardPointTransactionDao {
    @Override
    public String getName() {
        return "RewardPointTransaction";
    }

    private final RewardPointTransactionRepository repo;

    @Override
    public Optional<RewardPointTransaction> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public RewardPointTransaction save(RewardPointTransaction entity) {
        return repo.save(entity);
    }

    @Override
    public Collection<RewardPointTransaction> saveAll(Collection<RewardPointTransaction> entities) {
        return repo.saveAll(entities);
    }

    @Override
    public void delete(RewardPointTransaction entity) {
        // Ledger is append-only: deleting is not allowed by design.
        throw new UnsupportedOperationException("RewardPointTransaction is append-only and cannot be deleted");
    }

    @Override
    public long sumPointsByVolunteer(String volunteerId) {
        return repo.sumPointsByVolunteer(volunteerId);
    }

    @Override
    public long sumPointsByVolunteerAndEvent(String volunteerId, long eventId) {
        return repo.sumPointsByVolunteerAndEvent(volunteerId, eventId);
    }

    @Override
    public Collection<EventPointsDto> sumPointsGroupedByEvent(String volunteerId) {
        return RewardPointsMapper.toEventPointsDto(repo.sumPointsGroupedByEvent(volunteerId));
    }

    public Collection<RewardPointTransaction> findAllByVolunteerIdOrderByCreatedAtAsc(String volunteerId) {
        return repo.findAllByVolunteerIdOrderByCreatedAtAsc(volunteerId);
    }
}
