package de.bht_berlin.paf.cash_flow_recorder.api;

import de.bht_berlin.paf.cash_flow_recorder.entity.Account;
import de.bht_berlin.paf.cash_flow_recorder.entity.Household;
import de.bht_berlin.paf.cash_flow_recorder.entity.HouseholdMember;
import de.bht_berlin.paf.cash_flow_recorder.service.HouseholdMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for the entity HouseholdMember
 */
@RestController
@RequestMapping(path = "/householdMember")
public class HouseholdMemberController {

    @Autowired
    private HouseholdMemberService householdMemberService;

    private final Logger logger = LoggerFactory.getLogger(HouseholdMemberController.class);

    // CRUD-Operations for the entity HouseholdMember

    /**
     * Create or update a householdMember (CREATE / UPDATE)
     * @param householdMember A householdMember to be create or update
     * @return The saved householdMember
     */
    @PatchMapping(path = "/upsert", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public HouseholdMember upsert(@RequestBody final HouseholdMember householdMember) {
        HouseholdMember savedHouseholdMember = householdMemberService.upsertHouseholdMember(householdMember);
        if (savedHouseholdMember == null) {
            logger.warn("HTTP-Response: Can't upsert householdMember ");
        } else {
            logger.info("HTTP-Response: Upserted householdMember id " + savedHouseholdMember.getId());
        }
        return savedHouseholdMember;
    }

    /**
     * Find all householdMembers (READ).
     * @return A list of HouseholdMembers
     */
    @GetMapping
    @CrossOrigin
    public List<HouseholdMember> getAllHouseholdMembers() {
        List<HouseholdMember> householdMemberList = householdMemberService.findHouseholdMember(null);
        if (householdMemberList.isEmpty()) {
            logger.warn("HTTP-Response: Can't find any householdMembers");
        } else {
            logger.info("HTTP-Response: Found " + householdMemberList.size());
        }
        return householdMemberList;
    }

    /**
     * Find a householdMember by id (READ).
     * @param id Id of the HouseholdMember to be found
     * @return A list of founded HouseholdMembers
     */
    @GetMapping(path = "/{id}")
    @CrossOrigin
    public List<HouseholdMember> getHouseholdMember(@PathVariable("id") final Long id) {
        List<HouseholdMember> householdMemberList = householdMemberService.findHouseholdMember(id);
        if (householdMemberList.isEmpty()) {
            logger.warn("HTTP-Response: Can't find householdMember with id " + id);
        } else {
            logger.info("HTTP-Response: Found " + householdMemberList.size() + " householdMember with id " + id);
        }
        return householdMemberList;
    }

    /**
     * Delete a householdMember by id (DELETE).
     * @param id ID of the HouseholdMember to be deleted
     * @return A list of deleted HouseholdMembers
     */
    @DeleteMapping(path = "/{id}")
    @CrossOrigin
    public ResponseEntity<String> deleteHouseholdMember(@PathVariable("id") final Long id) {
        List<HouseholdMember> householdMemberList = householdMemberService.deleteHouseholdMember(id);
        if (householdMemberList.isEmpty()) {
            logger.warn("HTTP-Response: Can't delete householdMember with id " + id);
            return ResponseEntity.notFound().build();
        } else {
            logger.info("HTTP-Response: Deleted householdMember with id " + id);
            return ResponseEntity.ok("Entity with ID " + id + " was deleted.");
        }
    }

    // Use-Case-Operations for the entity HouseholdMember

    /**
     * Added a household to a given householdMember by id
     * @param id The id of the householdMember
     * @param household A household to be saved (create or update)
     * @return The saved householdMember
     */
    @PatchMapping(path = "/{id}/addHousehold", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public HouseholdMember addHousehold(@PathVariable("id") final Long id, @RequestBody final Household household) {
        HouseholdMember savedHouseholdMember = householdMemberService.addHousehold(id, household);
        if (savedHouseholdMember == null) {
            logger.warn("HTTP-Response: Can't add household to householdMember with id " + id);
        } else {
            logger.info("HTTP-Response: Added household to householdMember with id " + id);
        }
        return savedHouseholdMember;
    }

    /**
     * Added an account to a given household and householdMember by id
     * @param id The id of the householdMember
     * @param householdId The id of the household
     * @param account An account to be saved (create or update)
     * @return The saved householdMember
     */
    @PatchMapping(path = "/{id}/addAccount", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public HouseholdMember addAccount(@PathVariable("id") final Long id, @RequestParam("household") final Long householdId, @RequestBody final Account account) {
        HouseholdMember savedHouseholdMember = householdMemberService.addAccount(id, householdId, account);
        if (savedHouseholdMember == null) {
            logger.warn("HTTP-Response: Can't add account to householdMember with id " + id);
        } else {
            logger.info("HTTP-Response: Added account to householdMember with id " + id);
        }
        return savedHouseholdMember;
    }

    /**
     * Get all saved households to a given householdMember by id
     * @param id The id of the householdMember
     * @return A list of households
     */
    @GetMapping(path = "/{id}/households")
    @CrossOrigin
    public List<Household> getHouseholdsFromHouseholdMember(@PathVariable("id") final Long id) {
        List<Household> householdList = householdMemberService.getHouseholds(id);
        if (householdList.isEmpty()) {
            logger.warn("HTTP-Response: Can't get households from householdMember with id " + id);
        } else {
            logger.info("HTTP-Response: Found " + householdList.size() + " households from householdMember with id " + id);
        }
        return householdList;
    }

    /**
     * Get all saved accounts to a given householdMember by id
     * @param id The id of the householdMember
     * @return A list of accounts
     */
    @GetMapping(path = "/{id}/accounts")
    @CrossOrigin
    public List<Account> getAccountsFromHouseholdMember(@PathVariable("id") final Long id) {
        List<Account> accountList = householdMemberService.getAccounts(id);
        if (accountList.isEmpty()) {
            logger.warn("HTTP-Response: Can't get accounts from householdMember with id " + id);
        } else {
            logger.info("HTTP-Response: Found " + accountList.size() + " accounts from householdMember with id " + id);
        }
        return householdMemberService.getAccounts(id);
    }
}
