package cash_flow_recorder.entity.state;
import cash_flow_recorder.entity.ReceiptCopy;
import cash_flow_recorder.service.OCRService;
import cash_flow_recorder.service.ReceiptCopyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete state class for the state pattern (STATE: TRANSLATED)
 */
public class TranslatedReceiptCopyState implements ReceiptCopyState {

    private final Logger logger = LoggerFactory.getLogger(ReceiptCopyService.class);

    /**
     * Method of starting the automation process for categorization
     * @param receiptCopy The concrete receiptCopy
     * @param ocrService The implemented OCR-Engine as Service
     */
    @Override
    public void process(ReceiptCopy receiptCopy, OCRService ocrService) {
        logger.info("Receipt is already translated.");
        // ToDo - Implement next state for the automatic categorization
    }
}