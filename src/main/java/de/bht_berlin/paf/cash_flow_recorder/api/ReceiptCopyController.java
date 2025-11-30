package de.bht_berlin.paf.cash_flow_recorder.api;

import de.bht_berlin.paf.cash_flow_recorder.entity.ReceiptCopy;
import de.bht_berlin.paf.cash_flow_recorder.service.ReceiptCopyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Controller for the entity ReceiptCopy
 */
@RestController
@RequestMapping(path = "/receiptCopy")
public class ReceiptCopyController {

    @Autowired
    private ReceiptCopyService receiptCopyService;

    private final Logger logger = LoggerFactory.getLogger(ReceiptCopyController.class);

    // CRUD-Operations for the entity
    // CREATE / UPDATE over the Expenditure

    /**
     * Create or update an receiptCopy (CREATE / UPDATE)
     * @param receiptCopy The receiptCopy to be create or update
     * @return The saved receiptCopy
     */
    @PatchMapping(path = "/upsert", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ReceiptCopy upsert(@RequestBody final ReceiptCopy receiptCopy) {
        ReceiptCopy savedReceiptCopy = receiptCopyService.upsertReceiptCopy(receiptCopy);
        return savedReceiptCopy;
    }

    /**
     * Find all receiptCopies (READ)
     * @return A list of receiptCopies
     */
    @GetMapping
    public List<ReceiptCopy> getAllReceiptCopies() {
        List<ReceiptCopy> receiptCopyList = receiptCopyService.findReceiptCopy(null);
        return receiptCopyList;
    }

    /**
     * Find an receiptCopy by id (READ)
     * @param id The id of the receiptCopy to be found
     * @return A list of founded receiptCopies
     */
    @GetMapping(path = "/{id}")
    public List<ReceiptCopy> getReceiptCopyById(@PathVariable("id") final Long id) {
        List<ReceiptCopy> receiptCopyList = receiptCopyService.findReceiptCopy(id);
        return receiptCopyList;
    }

    /**
     * Delete an receiptCopy by id (DELETE)
     * @param id The id of the receiptCopy to be deleted
     * @return The Response HTTP Status
     */
    @DeleteMapping(path = "/{id}")
    @CrossOrigin
    public ResponseEntity<String> deleteReceiptCopy(@PathVariable("id") final Long id) {
        List<ReceiptCopy> receiptCopyList = receiptCopyService.deleteReceiptCopy(id);
        if (receiptCopyList.isEmpty()) {
            logger.warn("HTTP-Response: ReceiptCopy with id " + id + " can't deleted");
            return ResponseEntity.notFound().build();
        } else {
            logger.info("HTTP-Response: ReceiptCopy deleted with id " + id);
            return ResponseEntity.ok("ReceiptCopy with id " + id + " was deleted");
        }
    }

    // Use-Case-Operations for the entity Household

    /**
     * Get image of receiptCopy by id (READ)
     * @param id The id of the receiptCopy
     * @return Image as ResponseEntity<Byte[]>
     */
    @GetMapping(path = "/{id}/image", produces = MediaType.IMAGE_JPEG_VALUE)
    @CrossOrigin
    public ResponseEntity<byte[]> getReceiptImageById(@PathVariable("id") Long id) {
        try {
            byte[] imageData = receiptCopyService.findReceiptCopyImage(id);
            logger.info("HTTP-Response: Image for receiptCopy id " + id + " was found");
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(imageData);
        } catch (FileNotFoundException e) {
            logger.warn("HTTP-Response: Image for receiptCopy with id " + id + " could not be found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IOException e) {
            logger.error("HTTP-Response: Image for receiptCopy with id " + id + " could not be found by INTERNAL SERVER ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Change the state of the receiptCopy and save it on DB
     * @param id The id of the receiptCopy
     * @return The response HTTP Status.
     */
    @GetMapping("/{id}/translate")
    @CrossOrigin
    public ResponseEntity<String> translateReceipt(@PathVariable Long id) {
        List<ReceiptCopy> receiptCopy = receiptCopyService.findReceiptCopy(id);
        if (receiptCopy.isEmpty()) {
            logger.warn("HTTP-Response: ReceiptCopy with id " + id + " could not be found");
            return ResponseEntity.notFound().build();
        } else {
            ReceiptCopy savedReceiptCopy = receiptCopyService.processReceipt(receiptCopy.getFirst());
            logger.info("HTTP-Response: ReceiptCopy with id " + id + " was translated");
            return ResponseEntity.ok(savedReceiptCopy.getTranslation());
        }
    }
}
