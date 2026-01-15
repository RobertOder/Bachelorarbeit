package cash_flow_recorder.api;

import cash_flow_recorder.entity.Account;
import cash_flow_recorder.entity.Expenditure;
import cash_flow_recorder.entity.ExpenditureCategory;
import cash_flow_recorder.entity.ReceiptCopy;
import cash_flow_recorder.service.ExpenditureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller for the entity Expenditure
 */
@RestController
@RequestMapping(path = "/expenditure")
public class ExpenditureController {

    @Autowired
    private ExpenditureService expenditureService;

    private final Logger logger = LoggerFactory.getLogger(ExpenditureController.class);

    // CRUD-Operations for the entity Expenditure
    // CREATE / UPDATE over the Account

    /**
     * Create or update an expenditure (CREATE / UPDATE)
     * @param expenditure The expenditure to be create or update
     * @return The saved expenditure
     */
    @PatchMapping(path = "/upsert", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Expenditure upsert(@RequestBody final Expenditure expenditure) {
        Expenditure savedExpendisture = expenditureService.upsertExpenditure(expenditure);
        return savedExpendisture;
    }

    /**
     * Find all expenditures (READ)
     * @return A list of expenditures
     */
    @GetMapping
    @CrossOrigin
    public List<Expenditure> getAllExpenditures(@RequestParam(value = "householdMember", required = false) final Long id) {
        List<Expenditure> expenditureList;
        if (id == null) {
            logger.info("HTTP-Response: Get all expenditures");
            expenditureList = expenditureService.findExpenditure(null);
        } else {
            logger.info("HTTP-Response: Get all expenditures for householdMember with id " + id);
            expenditureList = expenditureService.findExpenditureByHouseholdMember(id);
        }
        return expenditureList;
    }

    /**
     * Find an expenditure by id (READ)
     * @param id The id of the expenditure to be found
     * @return A list of founded expenditure
     */
    @GetMapping(path = "/{id}")
    public List<Expenditure> getExpenditureById(@PathVariable("id") final Long id) {
        List<Expenditure> expenditureList = expenditureService.findExpenditure(id);
        return expenditureList;
    }

    /**
     * Delete an expenditure by id (DELETE)
     * @param id The id of the expenditure to be deleted
     * @return The Response HTTP Status
     */
    @DeleteMapping(path = "/{id}")
    @CrossOrigin
    public ResponseEntity<String> deleteExpenditure(@PathVariable("id") final Long id) {
        List<Expenditure> expenditureList = expenditureService.deleteExpenditure(id);
        if (expenditureList.isEmpty()) {
            logger.warn("HTTP-Response: Can't delete expenditure with id " + id);
            return ResponseEntity.notFound().build();
        } else {
            logger.info("HTTP-Response: Delete expenditure with id " + id);
            return ResponseEntity.ok("Expenditure with id " + id + " was deleted");
        }
    }

    // Use-Case-Operations for the entity Expenditure

    /**
     * Get an account by expenditure id
     * @param id The id of the expenditure
     * @return The founded account
     */
    @GetMapping(path = "/{id}/account")
    @CrossOrigin
    public Account getAccountByExpenditureId(@PathVariable("id") final Long id) {
        Account expenditureAccount = expenditureService.getAccount(id);
        if (expenditureAccount == null) {
            logger.warn("HTTP-Response: Account with expenditure id " + id + " not found.");
        } else {
            logger.info("HTTP-Response: Get expenditure account with id " + id);
        }
        return expenditureAccount;
    }

    /**
     * Get an expenditureCategory by expenditure id
     * @param id The id of the expenditure
     * @return The founded expenditureCategory
     */
    @GetMapping(path = "/{id}/expenditureCategory")
    @CrossOrigin
    public ExpenditureCategory getExpenditureCategoryByExpenditureId(@PathVariable("id") final Long id) {
        ExpenditureCategory expenditureCategory = expenditureService.getExpenditureCategory(id);
        if (expenditureCategory == null) {
            logger.warn("HTTP-Response: expenditureCategory for expenditure id " + id + " not found.");
        } else {
            logger.info("HTTP-Response: Get expenditureCategory for expenditure id " + id);
        }
        return expenditureCategory;
    }

    /**
     * Get the receiptCopy by expenditure id
     * @param id The id of the expenditure
     * @return The founded receiptCopy
     */
    @GetMapping(path = "/{id}/receiptCopies")
    @CrossOrigin
    public List<ReceiptCopy> getReceiptCopiesByExpenditureId(@PathVariable("id") final Long id) {
        List<ReceiptCopy> receiptCopies = expenditureService.getReceiptCopies(id);
        if (receiptCopies.isEmpty()) {
            logger.warn("HTTP-Response: No receiptCopies for expenditure id " + id + " found.");
        } else {
            logger.info("HTTP-Response: ReceiptCopies for expenditure id " + id + " found.");
        }
        return receiptCopies;
    }

    /**
     * Added a receiptCopy to a given expenditure by id
     * @param id The id of the expenditure
     * @param file The file of the receiptCopy to be saved (create or update)
     * @return The saved Expenditure
     */
    @PatchMapping(path = "/{id}/addReceiptCopy", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public Expenditure addReceiptCopy(@PathVariable("id") final Long id, @RequestPart(value = "photoFile", required = true) final MultipartFile file) {
        Expenditure savedExpenditure = expenditureService.addReceiptCopy(id, file);
        if (savedExpenditure == null) {
            logger.warn("HTTP-Response: Can't add receiptCopy for expenditure id " + id);
        } else {
            logger.info("HTTP-Response: Added receiptCopy for expenditure id " + id);
        }
        return savedExpenditure;
    }

    /**
     * Assign a expenditureCategory to a given expenditure by id
     * @param id The id of the expenditure
     * @param categoryId The id of the expenditureCategory (create or update)
     * @return The saved Expentiture
     */
    @PatchMapping(path = "/{id}/assignCategory", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public Expenditure assignCategory(@PathVariable("id") final Long id, @RequestParam("expenditureCategory") final Long categoryId) {
        Expenditure savedExpenditure = expenditureService.assignCategory(id, categoryId);
        if (savedExpenditure == null) {
            logger.warn("HTTP-Response: Can't assign expenditure with id " + id + " to expenditureCategory " + categoryId);
        } else {
            logger.info("HTTP-Response: Expenditure with id " + id + " assigned to expenditureCategory " + categoryId);
        }
        return savedExpenditure;
    }

}
