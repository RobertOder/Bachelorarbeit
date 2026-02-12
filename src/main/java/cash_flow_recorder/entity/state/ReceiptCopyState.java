package cash_flow_recorder.entity.state;

import cash_flow_recorder.entity.ReceiptCopy;
import cash_flow_recorder.service.OCRService;
import cash_flow_recorder.service.ReceiptCopyService;
import cash_flow_recorder.service.ExpenditureService;

/**
 * Interface for the state pattern
 */
public interface ReceiptCopyState {
    /**
     * Method of starting the automation process for categorization
     * @param ocrService The implemented OCR-Engine as Service
     * @param receiptCopyService Implemented ReceiptCopy-Service
     * @param receiptCopy The context class
     */
    void process(ReceiptCopy receiptCopy, OCRService ocrService, ReceiptCopyService receiptCopyService,
                 ExpenditureService expenditureService);
}
