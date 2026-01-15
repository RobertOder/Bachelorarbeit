package cash_flow_recorder.repo;

import cash_flow_recorder.entity.Account;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for the entity account
 */
public interface AccountRepo extends CrudRepository<Account, Long> {
}
