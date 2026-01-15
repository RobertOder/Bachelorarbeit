package cash_flow_recorder.api;

import cash_flow_recorder.entity.ExpenditureCategory;
import cash_flow_recorder.service.ExpenditureCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for the entity ExpenditureCategory
 */
@RestController
@RequestMapping(path = "/expenditureCategory")
public class ExpenditureCategoryController {

    @Autowired
    private ExpenditureCategoryService expenditureCategoryService;

    private final Logger logger = LoggerFactory.getLogger(ExpenditureCategoryController.class);

    // CRUD-Operations for the entity ExpenditureCategory
    // CREATE / UPDATE over the Household

    /**
     * Create or update a expenditureCategory (CREATE / UPDATE)
     * @param expenditureCategory The expenditureCategory to be create or update
     * @return The saved expenditureCategory
     */
    @PatchMapping(path = "/upsert", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ExpenditureCategory upsert(@RequestBody final ExpenditureCategory expenditureCategory) {
        ExpenditureCategory savedExpenditureCategory = expenditureCategoryService.upsertExpenditureCategory(expenditureCategory);
        return savedExpenditureCategory;
    }

    /**
     * Find all expenditureCategories (READ)
     * @return A list of expenditureCategories
     */
    @GetMapping
    public List<ExpenditureCategory> getAllExpenditureCategories() {
        List<ExpenditureCategory> expenditureCategoryList = expenditureCategoryService.findExpenditureCategory(null);
        return expenditureCategoryList;
    }

    /**
     * Find an expenditureCategory by id (READ)
     * @param id The id of the expenditureCategory to be found
     * @return A list of founded expenditureCategories
     */
    @GetMapping(path = "/{id}")
    public List<ExpenditureCategory> getExpenditureCategoryById(@PathVariable("id") final Long id) {
        List<ExpenditureCategory> expenditureCategoryList = expenditureCategoryService.findExpenditureCategory(id);
        return expenditureCategoryList;
    }

    /**
     * Delete an expenditureCategory by id (DELETE)
     * @param id The id of the expenditureCategory to be deleted
     * @return The Response HTTP Status
     */
    @DeleteMapping(path = "/{id}")
    @CrossOrigin
    public ResponseEntity<String> deleteExpenditureCategory(@PathVariable("id") final Long id) {
        List<ExpenditureCategory> expenditureCategoryList = expenditureCategoryService.deleteExpenditureCategory(id);
        if (expenditureCategoryList.isEmpty()) {
            logger.warn("HTTP-Response: Can't delete expenditureCategory with id " + id);
            return ResponseEntity.notFound().build();
        } else {
            logger.info("HTTP-Response: ExpenditureCategory with id " + id + " deleted.");
            return ResponseEntity.ok("ExpenditureCategory with id " + id + " was deleted");
        }
    }

    // Use-Case-Operations for the entity Expenditure

    /**
     * Returns the cost of the given category for the current month.
     * @param id The id of the expenditureCategory (READ)
     * @return The String of current cost as response HTTP Status
     */
    @GetMapping(path = "/{id}/currentMonth")
    @CrossOrigin
    public ResponseEntity<String>  getCurrentMonth(@PathVariable("id") final Long id) {
        String currentMonth = expenditureCategoryService.getCurrentMonth(id);
        if (currentMonth.isEmpty()) {
            logger.warn("HTTP-Response: Cost of current month for expenditureCategory id " + id + " is empty.");
            return ResponseEntity.notFound().build();
        } else {
            logger.info("HTTP-Response: Current month for expenditureCategory id " + id + " found. Value:" + currentMonth);
            return ResponseEntity.ok(currentMonth);
        }
    }
}
