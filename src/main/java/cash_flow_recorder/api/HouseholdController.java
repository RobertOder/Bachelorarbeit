package cash_flow_recorder.api;

import cash_flow_recorder.entity.ExpenditureCategory;
import cash_flow_recorder.entity.Household;
import cash_flow_recorder.service.HouseholdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for the entity Household
 */
@RestController
@RequestMapping(path = "/household")
public class HouseholdController {

    @Autowired
    private HouseholdService householdService;

    private final Logger logger = LoggerFactory.getLogger(HouseholdController.class);

    // CRUD-Operations for the entity Household
    // CREATE / UPDATE over the HouseholdMember

    /**
     * Create or update a household (CREATE / UPDATE)
     * @param household The household to be create or update
     * @return The saved household
     */
    @PatchMapping(path = "/upsert", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Household upsert(@RequestBody final Household household) {
        Household savedHousehold = householdService.upsertHousehold(household);
        return savedHousehold;
    }

    /**
     * Find all households (READ).
     * @return A list of households
     */
    @GetMapping
    @CrossOrigin
    public List<Household> getAllHouseholds() {
        List<Household> householdList = householdService.findHousehold(null);
        if (householdList.isEmpty()) {
            logger.warn("HTTP-Response: Can't get all households");
        } else {
            logger.info("HTTP-Response: Found " + householdList.size() + " households");
        }
        return householdList;
    }

    /**
     * Find a household by id (READ).
     * @param id The id of the household to be found
     * @return A list of founded households
     */
    @GetMapping(path = "/{id}")
    @CrossOrigin
    public List<Household> getHousehold(@PathVariable("id") final Long id) {
        List<Household> householdList = householdService.findHousehold(id);
        if (householdList.isEmpty()) {
            logger.warn("HTTP-Response: Can't get household with id " + id);
        } else {
            logger.info("HTTP-Response: Found household with id " + id);
        }
        return householdList;
    }

    /**
     * Delete a household by id (DELETE).
     * @param id The id of the household to be deleted
     * @return A list of deleted households
     */
    @DeleteMapping("/{id}")
    @CrossOrigin
    public ResponseEntity<String> deleteHousehold(@PathVariable("id") final Long id) {
        List<Household> householdList = householdService.deleteHousehold(id);
        if (householdList.isEmpty()) {
            logger.warn("HTTP-Response: Can't delete household with id " + id);
            return ResponseEntity.notFound().build();
        } else {
            logger.info("HTTP-Response: Deleted household with id " + id);
            return ResponseEntity.ok("Household with id " + id + " was deleted.");
        }
    }

    // Use-Case-Operations for the entity Household

    /**
     * Added a expenditureCategory to a given household by id
     * @param id The id of the household
     * @param expenditureCategory The expentidireCategory to be saved (create or update)
     * @return The saved household
     */
    @PatchMapping(path = "/{id}/addExpenditureCategory", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public Household addExpenditureCategory(@PathVariable("id") final Long id, @RequestBody final ExpenditureCategory expenditureCategory) {
        Household savedHousehold = householdService.addExpenditureCategory(id, expenditureCategory);
        if (savedHousehold == null) {
            logger.warn("HTTP-Response: Can't add expenditureCategory for household id " + id);
        } else {
            logger.info("HTTP-Response: Added expenditureCategory for household id " + id);
        }
        return savedHousehold;
    }

    /**
     * Find expenditureCategories by household id (READ).
     * @param id The id of the household
     * @return A list of founded expenditureCategories
     */
    @GetMapping(path = "/{id}/expenditureCategories")
    @CrossOrigin
    public List<ExpenditureCategory> getExpenditureCategoriesFromHousehold(@PathVariable("id") final Long id) {
        List<ExpenditureCategory> expenditureCategoryList = householdService.getExpenditureCategories(id);
        if (expenditureCategoryList.isEmpty()) {
            logger.warn("HTTP-Response: Can't get expenditureCategories for household id " + id);
        } else {
            logger.info("HTTP-Response: Found " + expenditureCategoryList.size() + " expenditureCategories for household id " + id);
        }
        return expenditureCategoryList;
    }
}
