package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.Optional;

import at.shiftcontrol.shiftservice.dao.RewardPointsTransactionDao;
import at.shiftcontrol.shiftservice.dto.rewardpoints.EventPointsDto;
import at.shiftcontrol.shiftservice.entity.RewardPointsTransaction;
import at.shiftcontrol.shiftservice.mapper.RewardPointsMapper;
import at.shiftcontrol.shiftservice.repo.RewardPointTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RewardPointsTransactionDaoImpl implements RewardPointsTransactionDao {
    @Override
    public String getName() {
        return "RewardPointTransaction";
    }

    private final RewardPointTransactionRepository repo;

    @Override
    public Optional<RewardPointsTransaction> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public RewardPointsTransaction save(RewardPointsTransaction entity) {
        return repo.save(entity);
    }

    @Override
    public Collection<RewardPointsTransaction> saveAll(Collection<RewardPointsTransaction> entities) {
        return repo.saveAll(entities);
    }

    @Override
    public void delete(RewardPointsTransaction entity) {
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

    public Collection<RewardPointsTransaction> findAllByVolunteerIdOrderByCreatedAtAsc(String volunteerId) {
        return repo.findAllByVolunteerIdOrderByCreatedAtAsc(volunteerId);
    }
}
