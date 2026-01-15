package cash_flow_recorder.repo;

import cash_flow_recorder.entity.Income;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for the entity income
 */
public interface IncomeRepo extends CrudRepository<Income, Long> {
}
