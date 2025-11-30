package de.bht_berlin.paf.cash_flow_recorder.repo;

import de.bht_berlin.paf.cash_flow_recorder.entity.HouseholdMember;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for the entity householdMember
 */
public interface HouseholdMemberRepo extends CrudRepository<HouseholdMember, Long> {
}
