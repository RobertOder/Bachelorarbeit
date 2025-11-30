package de.bht_berlin.paf.cash_flow_recorder.repo;

import de.bht_berlin.paf.cash_flow_recorder.entity.Income;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for the entity income
 */
public interface IncomeRepo extends CrudRepository<Income, Long> {
}
