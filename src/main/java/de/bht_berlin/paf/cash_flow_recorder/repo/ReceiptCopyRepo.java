package de.bht_berlin.paf.cash_flow_recorder.repo;

import de.bht_berlin.paf.cash_flow_recorder.entity.ReceiptCopy;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for the entity receiptCopy
 */
public interface ReceiptCopyRepo extends CrudRepository<ReceiptCopy, Long> {
}
