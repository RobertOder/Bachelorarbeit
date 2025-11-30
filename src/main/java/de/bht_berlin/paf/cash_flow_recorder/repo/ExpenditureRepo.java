package de.bht_berlin.paf.cash_flow_recorder.repo;

import de.bht_berlin.paf.cash_flow_recorder.entity.Expenditure;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for the entity expenditure
 */
public interface ExpenditureRepo extends CrudRepository<Expenditure, Long> {
}
