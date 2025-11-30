package de.bht_berlin.paf.cash_flow_recorder.repo;

import de.bht_berlin.paf.cash_flow_recorder.entity.Household;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for the entity household
 */
public interface HouseholdRepo extends CrudRepository<Household, Long> {
}
