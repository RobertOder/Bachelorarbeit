package de.bht_berlin.paf.cash_flow_recorder.entity.state;

import de.bht_berlin.paf.cash_flow_recorder.entity.ReceiptCopy;
import de.bht_berlin.paf.cash_flow_recorder.service.OCRService;

/**
 * Interface for the state pattern
 */
public interface ReceiptCopyState {
    /**
     * Method of starting the automation process for categorization
     * @param ocrService The implemented OCR-Engine as Service
     * @param receiptCopy The context class
     */
    void process(ReceiptCopy receiptCopy, OCRService ocrService);
}
