package cash_flow_recorder.repo;

import cash_flow_recorder.entity.ReceiptCopy;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for the entity receiptCopy
 */
public interface ReceiptCopyRepo extends CrudRepository<ReceiptCopy, Long> {
}
