package cash_flow_recorder.repo;

import cash_flow_recorder.entity.ExpenditureCategory;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for the entity expenditureCategoryRepo
 */
public interface ExpenditureCategoryRepo extends CrudRepository<ExpenditureCategory, Long> {
}
