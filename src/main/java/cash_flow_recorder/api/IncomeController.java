package cash_flow_recorder.api;

import cash_flow_recorder.entity.Income;
import cash_flow_recorder.service.IncomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for the entity Income
 */
@RestController
@RequestMapping(path = "/income")
public class IncomeController {
    @Autowired
    private IncomeService incomeService;

    // CRUD-Operations for the entity Income
    // CREATE / UPDATE over the Account

    /**
     * Create or update an income (CREATE / UPDATE)
     * @param income The income to be create or update
     * @return The saved income
     */
    @PatchMapping(path = "/upsert", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Income upsertIncome(@RequestBody final Income income) {
        Income savedIncome = incomeService.upsertIncome(income);
        return savedIncome;
    }

    /**
     * Find all incomes (READ)
     * @return A list of incomes
     */
    @GetMapping
    public List<Income> getAllIncomes() {
        List<Income> incomeList = incomeService.findIncome(null);
        return incomeList;
    }

    /**
     * Find an income by id (READ)
     * @param id The id of the income to be found
     * @return A list of founded incomes
     */
    @GetMapping(path = "/{id}")
    public List<Income> getIncomeById(@PathVariable("id") final Long id) {
        List<Income> incomeList = incomeService.findIncome(id);
        return incomeList;
    }

    /**
     * Delete an income by id (DELETE)
     * @param id The id of the income to be deleted
     * @return The Response HTTP Status
     */
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<String> deleteIncome(@PathVariable("id") final Long id) {
        List<Income> incomeList = incomeService.deleteIncome(id);
        if (incomeList.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok("Income with id " + id + " was deleted");
        }
    }
}
