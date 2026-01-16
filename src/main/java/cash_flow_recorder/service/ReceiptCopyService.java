package cash_flow_recorder.service;

import cash_flow_recorder.entity.*;
import cash_flow_recorder.entity.*;
import cash_flow_recorder.repo.ReceiptCopyRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Service-Class for the entity receiptCopy
 */
@Service
public class ReceiptCopyService {

    private final ReceiptCopyRepo receiptCopyRepo;
    private final OCRService ocrService;
    private final Logger logger = LoggerFactory.getLogger(ReceiptCopyService.class);

    @Autowired // Constructor Injection
    public ReceiptCopyService(ReceiptCopyRepo receiptCopyRepo, OCRService ocrService) {
        this.receiptCopyRepo = receiptCopyRepo;
        this.ocrService = ocrService;
    }

    // CRUD-Operations for the entity receiptCopy
    // Creation over the entity expenditures

    /**
     * Save or update an receiptCopy (upsert = update + insert)
     * @param receiptCopy ReceiptCopy to be saved (CREATE / UPDATE)
     * @return Created or updated receiptCopy
     */
    public ReceiptCopy upsertReceiptCopy(ReceiptCopy receiptCopy) {
        ReceiptCopy savedReceiptCopy = receiptCopyRepo.save(receiptCopy);
        if(savedReceiptCopy == null) {
            logger.error("Service: Saving income failed");
            throw new RuntimeException("Saving income failed");
        }
        logger.info("Service: Saving income successful with id " + savedReceiptCopy.getId());
        return savedReceiptCopy;
    }

    /**
     * Find all receiptCopies or one receiptCopy by id
     * @param id The id of the receiptCopy to be found (READ)
     * @return List of receiptCopies
     */
    public List<ReceiptCopy> findReceiptCopy(Long id) {
        List<ReceiptCopy> receiptCopyList = new ArrayList<>();
        if (id == null) {
            receiptCopyRepo.findAll().forEach(receiptCopyList::add);
            logger.info("Service: Get all receiptCopies.");
        } else {
            receiptCopyRepo.findById(id).ifPresent(receiptCopyList::add);
            logger.info("Service: Get receiptCopies successful with id " + id);
        }
        return receiptCopyList;
    }

    /**
     * Delete all receiptCopies or an receiptCopy by id
     * @param id The id of the receiptCopy to be deleted (DELETE)
     * @return List of deleted receiptCopies
     */
    public List<ReceiptCopy> deleteReceiptCopy(Long id) {
        List<ReceiptCopy> receiptCopyList = new ArrayList<>();
        if (id == null) {
            receiptCopyRepo.findAll().forEach(receiptCopyList::add);
            receiptCopyRepo.deleteAll();
            logger.warn("Service: Delete all receiptCopies.");
        } else {
            receiptCopyRepo.findById(id).ifPresent(receiptCopyList::add);
            receiptCopyRepo.deleteById(id);
            logger.info("Service: Delete receiptCopy successful with id " + id);
        }
        return receiptCopyList;
    }

    // Use-Case-Operations for the entity receiptCopy

    /**
     * Find image of receiptCopy by id
     * @param id The id of the receiptCopy
     * @return Image as Byte[]
     * @throws IOException If no image available
     */
    public byte[] findReceiptCopyImage(Long id) throws IOException {
        ReceiptCopy receiptCopy = receiptCopyRepo.findById(id)
                .orElseThrow(() -> new FileNotFoundException("ReceiptCopy not found with id: " + id));
        String imagePath = System.getProperty("user.dir") + receiptCopy.getPhotoPath();
        if (imagePath == null || imagePath.isEmpty()) {
            logger.error("Service: Image path is empty for receiptCopy with id: " + id);
            throw new FileNotFoundException("Image path is empty for receiptCopy with id: " + id);
        }
        Path path = Paths.get(imagePath);
        if (!Files.exists(path)) {
            logger.error("Service: Image file not found at path: " + imagePath);
            throw new FileNotFoundException("Image file not found at path: " + imagePath);
        }
        logger.info("Service: Get image successful with id " + id);
        return Files.readAllBytes(path);
    }

    /**
     * Process the OCR translation for a ReceiptCopy
     * @param receiptCopy ReceiptCopy to process
     * @return Updated receiptCopy
     */
    public ReceiptCopy processReceipt(ReceiptCopy receiptCopy) {
        receiptCopy.process(ocrService);
        ReceiptCopy savedReceiptCopy = receiptCopyRepo.save(receiptCopy);
        if(savedReceiptCopy == null) {
            logger.error("Service: Failed to save receiptCopy");
            throw new RuntimeException("Failed to save receiptCopy");
        }
        logger.info("Service: Saving receiptCopy successful with id " + savedReceiptCopy.getId());
        return savedReceiptCopy;
    }

    // Provisorisch/Testweise... mach ich noch richtig... z.B. globale Properties setzen.. villeicht auch über Spring-AI?
    public String processFindCategory(ReceiptCopy receiptCopy, Household household) {
        String url = "http://localhost:11434/api/generate";
        String posibleCategories = new String();
        if (receiptCopy.getStatus() == ReceiptCopyStatus.TRANSLATED) {
            if (household != null) {
                List<ExpenditureCategory> expenditureCategoryList = household.getExpenditureCategories();
                List<String> expenditureCategories = new ArrayList<>();
                for (ExpenditureCategory expenditureCategory : expenditureCategoryList) {
                    expenditureCategories.add(expenditureCategory.getName());
                }
                Expenditure expenditureOfReceipt = receiptCopy.getExpenditure();
                String article = expenditureOfReceipt.getArticle().toString();
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                String body = "{\"model\": \"qwen3:8b\", \"prompt\": \"Antworte nur in dem folgenden JSON-Format: (" +
                        " 'categories': <Haushaltskategorien auf Deutsch> ) Frage: Analysiere die Produkte eines" +
                        " Kassenbons, welcher mit OCR erfasst wurde:" + article + " Ich möchte in Bezug auf diese" +
                        " Produkte zwei Vorschläge zu möglichen übergeordneten" +
                        " Haushaltskategorien. Bitte Schlage eine Kategorie aus der folgenden Liste vor:" +
                        " " + expenditureCategories.toString() + " und eine Kategorie sollst du bitte" +
                        " komplett neu generieren.\", \"format\": \"json\", \"keep_alive\": \"0s\"," +
                        " \"options\": { \"temperature\": 0 }, \"stream\": false }";
                logger.info("AI-Request-Body sent: " + body);
                HttpEntity<String> entity = new HttpEntity<>(body, headers);
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                    posibleCategories = response.getBody();
                    logger.info("AI-Request-Body receive: " + posibleCategories);
                } catch (Exception ex) {
                    logger.error(ex.getMessage());
                }
            } else {
                Expenditure expenditureOfReceipt = receiptCopy.getExpenditure();
                String article = expenditureOfReceipt.getArticle().toString();
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                String body = "{\"model\": \"deepseek-r1:8b\", \"prompt\": \"Antworte nur in dem folgenden JSON-Format: (" +
                        " 'categories': <Haushaltskategorien auf Deutsch> ) Frage: Analysiere die Produkte eines" +
                        " Kassenbons, welcher mit OCR erfasst wurde:" + article + " Ich möchte in Bezug auf diese" +
                        " Produkte eine flache Liste mit Vorschlägen zu möglichen übergeordneten" +
                        " Haushaltskategorien. Diese Haushaltskategorien sollen einfach hintereinenader aufgelistet" +
                        " werden, ohne Fließtext.\", \"format\": \"json\", \"keep_alive\": \"0s\"," +
                        " \"options\": { \"temperature\": 0 }, \"stream\": false }";
                logger.info("AI-Request-Body sent: " + body);
                HttpEntity<String> entity = new HttpEntity<>(body, headers);
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                    posibleCategories = response.getBody();
                    logger.info("AI-Request-Body receive: " + posibleCategories);
                } catch (Exception ex) {
                    logger.error(ex.getMessage());
                }
            }
        } else {
            logger.warn("Service: Cant generate Categories, because Status of receiptCopy is not TRANSLATED");
        }
        return posibleCategories;
    }
}
