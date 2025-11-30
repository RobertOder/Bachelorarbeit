package de.bht_berlin.paf.cash_flow_recorder.service;

import de.bht_berlin.paf.cash_flow_recorder.entity.ReceiptCopy;
import de.bht_berlin.paf.cash_flow_recorder.entity.state.NewReceiptCopyState;
import de.bht_berlin.paf.cash_flow_recorder.repo.ReceiptCopyRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
