package de.bht_berlin.paf.cash_flow_recorder.repo;

import de.bht_berlin.paf.cash_flow_recorder.entity.Account;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for the entity account
 */
public interface AccountRepo extends CrudRepository<Account, Long> {
}
