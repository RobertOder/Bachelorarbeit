package cash_flow_recorder.api;

import cash_flow_recorder.entity.Account;
import cash_flow_recorder.entity.Expenditure;
import cash_flow_recorder.entity.Household;
import cash_flow_recorder.entity.Income;
import cash_flow_recorder.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for the entity Account
 */
@RestController
@RequestMapping(path = "/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    private final Logger logger = LoggerFactory.getLogger(AccountController.class);

    // CRUD-Operations for the entity HouseholdMember
    // CREATE / UPDATE over the HouseholdMember

    /**
     * Create or update an account (CREATE / UPDATE)
     * @param account The account to be create or update
     * @return The saved account
     */
    @PatchMapping(path = "/upsert", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Account upsert(@RequestBody final Account account) {
        Account savedAccount = accountService.upsertAccount(account);
        return savedAccount;
    }

    /**
     * Find all accounts (READ).
     * @return A list of accounts
     */
    @GetMapping
    public List<Account> getAllAccounts() {
        List<Account> accountList = accountService.findAccount(null);
        return accountList;
    }

    /**
     * Find an account by id (READ).
     * @param id The id of the account to be found
     * @return A list of founded accounts
     */
    @GetMapping(path = "/{id}")
    public List<Account> getAccount(@PathVariable("id") final Long id) {
        List<Account> accountList = accountService.findAccount(id);
        return accountList;
    }

    /**
     * Delete an account by id (DELETE).
     * @param id The id of the account to be deleted
     * @return A list of deleted accounts
     */
    @DeleteMapping(path = "/{id}")
    @CrossOrigin
    public ResponseEntity<String> deleteAccount(@PathVariable("id") final Long id) {
        List<Account> accountList = accountService.deleteAccount(id);
        if (accountList.isEmpty()) {
            logger.warn("HTTP-Response: Can't delete account with id " + id);
            return ResponseEntity.notFound().build();
        } else {
            logger.info("HTTP-Response: Account with id " + id + " deleted.");
            return ResponseEntity.ok("HTTP-Response: Account with id " + id + " was deleted");
        }
    }

    // Use-Case-Operations for the entity Account

    /**
     * Added an income to a given account by id
     * @param id The id of the account
     * @param income The income to be saved (create or update)
     * @return The saved account
     */
    @PatchMapping(path = "/{id}/addIncome", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public Income addIncome(@PathVariable("id") final Long id, @RequestBody final Income income) {
        Income savedIncome = accountService.addIncome(id, income);
        if (savedIncome == null) {
            logger.warn("HTTP-Response: Can't add income with account id " + id);
        } else {
            logger.info("HTTP-Response: Income for account id " + id + " added.");
        }
        return savedIncome;
    }

    /**
     * Added an expenditure to a given account by id
     * @param id The id of the account
     * @param expenditure The expenditure to be saved (create or update)
     * @return The saved account
     */
    @PatchMapping(path = "/{id}/addExpenditure", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public Expenditure addExpenditure(@PathVariable("id") final Long id, @RequestBody final Expenditure expenditure) {
        Expenditure savedExpenditure = accountService.addExpenditure(id, expenditure);
        if (savedExpenditure == null) {
            logger.warn("HTTP-Response: Can't add expenditure for account id " + id);
        } else {
            logger.info("HTTP-Response: Expenditure for account id " + id + " added.");
        }
        return savedExpenditure;
    }

    /**
     * Find a household by account id.
     * @param id The id of the account
     * @return Returns the allocated household (read)
     */
    @GetMapping(path = "/{id}/household")
    @CrossOrigin
    public Household getHousehold(@PathVariable("id") final Long id) {
        Household household = accountService.findAccount(id).getFirst().getHousehold();
        if (household == null) {
            logger.warn("HTTP-Response: Can't find household for account id " + id);
        } else {
            logger.info("HTTP-Response: Household for account id " + id + " found.");
        }
        return household;
    }
}
