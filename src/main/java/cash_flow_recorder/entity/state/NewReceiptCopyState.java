package cash_flow_recorder.entity.state;

import cash_flow_recorder.entity.*;
import cash_flow_recorder.service.ExpenditureService;
import cash_flow_recorder.service.OCRService;
import cash_flow_recorder.service.ReceiptCopyService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * Concrete state class for the state pattern (STATE: NEW)
 */
public class NewReceiptCopyState implements ReceiptCopyState {

    private final Logger logger = LoggerFactory.getLogger(ReceiptCopyService.class);

    /**
     * Method of starting the automation process for categorization
     * @param receiptCopy The concrete receiptCopy
     * @param ocrService The implemented OCR-Engine as Service
     */
    @Override
    public void process(ReceiptCopy receiptCopy, OCRService ocrService, ReceiptCopyService receiptCopyService,
                        ExpenditureService expenditureService) {
        // ToDo - Implements states for automated image preprocessing e.g. with OpenCV-Libs ?
        Expenditure expenditureOfReceipt = receiptCopy.getExpenditure();

        // OCR-Process
        ocrService.preprocess(System.getProperty("user.dir") + receiptCopy.getPhotoPath());
        List<List<String>> result = ocrService.performOcr(System.getProperty("user.dir") + receiptCopy.getPhotoPath());

        // Transmit details from OCR-Process
        receiptCopy.setTranslation(result.get(0).get(0));
        expenditureOfReceipt.setAmount(new BigDecimal(result.get(1).get(0).toString()));
        expenditureOfReceipt.setArticle(result.get(2));

        // Categorized
        Household household = expenditureOfReceipt.getAccount().getHousehold();
        String posibleCategories = receiptCopyService.processFindCategory(receiptCopy, household);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(posibleCategories);
            JsonNode responseNode = rootNode.path("response");
            //posibleCategories = responseNode.path("categories").get(0).asText();
            posibleCategories = responseNode.asText();
        } catch(Exception ex) {
            logger.error("Failed to parse String to JSON: " + ex.getMessage());
        }
        ObjectMapper objectMapperOwnCategorie  = new ObjectMapper();
        String ownCategorie = new String();
        try {
            JsonNode ownCatNode = objectMapperOwnCategorie.readTree(posibleCategories);
            JsonNode responseNode = ownCatNode.path("categories");
            ownCategorie = responseNode.get(0).asText();
        } catch (Exception ex) {
            logger.error("Failed to parse String to JSON: " + ex.getMessage());
        }
        logger.info("HTTP-Response: Found Categories for ReceiptCopy with id " + receiptCopy.getId() + " ");
        logger.info("Return KI-Answer: " + ownCategorie);
        for (ExpenditureCategory houseCats : household.getExpenditureCategories()) {
            if (houseCats.getName().equals(ownCategorie)) {
                expenditureService.assignCategory(expenditureOfReceipt.getId(),  houseCats.getId());
                logger.info("Assign expentiture ID: " + expenditureOfReceipt.getId() + " to Category ID: " + houseCats.getId());
            }
        }

        // Update status
        receiptCopy.setStatus(ReceiptCopyStatus.TRANSLATED);
        receiptCopy.setStateByStatus();
    }
}
