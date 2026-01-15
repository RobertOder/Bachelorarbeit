package cash_flow_recorder.service;

import cash_flow_recorder.entity.Income;
import cash_flow_recorder.repo.IncomeRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service-Class for the entity income
 */
@Service
public class IncomeService {

    private final IncomeRepo incomeRepo;
    private final Logger logger = LoggerFactory.getLogger(IncomeService.class);

    @Autowired // Constructor Injection
    public IncomeService(IncomeRepo incomeRepo) {
        this.incomeRepo = incomeRepo;
    }

    // CRUD-Operations for the entity income
    // Creation over the entity account

    /**
     * Save or update an income (upsert = update + insert)
     * @param income Income to be saved (CREATE / UPDATE)
     * @return Created or updated income
     */
    public Income upsertIncome(Income income) {
        Income savedIncome = incomeRepo.save(income);
        if (savedIncome == null) {
            logger.error("Failed to save income");
            throw new RuntimeException("Saving income failed");
        }
        logger.info("Service: Save income successful with id " + savedIncome.getId());
        return savedIncome;
    }

    /**
     * Find all incomes or one income by id
     * @param id The id of the income to be found (READ)
     * @return List of incomes
     */
    public List<Income> findIncome(Long id) {
        List<Income> incomeList = new ArrayList<>();
        if (id == null) {
            incomeRepo.findAll().forEach(incomeList::add);
            logger.info("Service: Get all incomes as list");
        } else {
            incomeRepo.findById(id).ifPresent(incomeList::add);
            logger.info("Service: Get incomes successful with id " + id);
        }
        return incomeList;
    }

    /**
     * Delete all incomes or an income by id
     * @param id The id of the income to be deleted (DELETE)
     * @return List of deleted incomes
     */
    public List<Income> deleteIncome(Long id) {
        List<Income> incomeList = new ArrayList<>();
        if (id == null) {
            incomeRepo.findAll().forEach(incomeList::add);
            incomeRepo.deleteAll();
            logger.warn("Service: Delete all incomes.");
        } else {
            incomeRepo.findById(id).ifPresent(incomeList::add);
            incomeRepo.deleteById(id);
            logger.info("Service: Delete income successful with id " + id);
        }
        return incomeList;
    }
}
