package cash_flow_recorder.entity.state;

import cash_flow_recorder.entity.Expenditure;
import cash_flow_recorder.entity.ReceiptCopy;
import cash_flow_recorder.entity.ReceiptCopyStatus;
import cash_flow_recorder.service.OCRService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Concrete state class for the state pattern (STATE: NEW)
 */
public class NewReceiptCopyState implements ReceiptCopyState {

    /**
     * Method of starting the automation process for categorization
     * @param receiptCopy The concrete receiptCopy
     * @param ocrService The implemented OCR-Engine as Service
     */
    @Override
    public void process(ReceiptCopy receiptCopy, OCRService ocrService) {
        // ToDo - Implements states for automated image preprocessing e.g. with OpenCV-Libs ?
        Expenditure expenditureOfReceipt = receiptCopy.getExpenditure();

        // OCR-Process
        ocrService.preprocess(System.getProperty("user.dir") + receiptCopy.getPhotoPath());
        List<List<String>> result = ocrService.performOcr(System.getProperty("user.dir") + receiptCopy.getPhotoPath());

        // Transmit details from OCR-Process
        receiptCopy.setTranslation(result.get(0).get(0));
        expenditureOfReceipt.setAmount(new BigDecimal(result.get(1).get(0).toString()));
        expenditureOfReceipt.setArticle(result.get(2));

        // Update status
        receiptCopy.setStatus(ReceiptCopyStatus.TRANSLATED);
        receiptCopy.setStateByStatus();
    }
}
