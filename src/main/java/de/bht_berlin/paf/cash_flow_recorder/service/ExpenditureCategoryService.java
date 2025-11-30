package de.bht_berlin.paf.cash_flow_recorder.service;

import de.bht_berlin.paf.cash_flow_recorder.entity.Expenditure;
import de.bht_berlin.paf.cash_flow_recorder.entity.ExpenditureCategory;
import de.bht_berlin.paf.cash_flow_recorder.repo.ExpenditureCategoryRepo;
import de.bht_berlin.paf.cash_flow_recorder.repo.ExpenditureRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Service-Class for the entity expenditureCategory
 */
@Service
public class ExpenditureCategoryService {

    private final ExpenditureCategoryRepo expenditureCategoryRepo;
    private final ExpenditureService expenditureService;
    private final ExpenditureRepo expenditureRepo;
    private final Logger logger = LoggerFactory.getLogger(ExpenditureCategoryService.class);

    @Autowired // Contructor Injection
    public ExpenditureCategoryService(ExpenditureCategoryRepo expenditureCategoryRepo, ExpenditureService expenditureService, ExpenditureRepo expenditureRepo) {
        this.expenditureCategoryRepo = expenditureCategoryRepo;
        this.expenditureService = expenditureService;
        this.expenditureRepo = expenditureRepo;
    }

    // CRUD-Operations for the entity expenditureCategory
    // Creation over the entity household

    /**
     * Save or update an expenditureCategory (upsert = update + insert)
     * @param expenditureCategory ExpenditureCategory to be saved (CREATE / UPDATE)
     * @return Created or updated expenditureCategory
     */
    public ExpenditureCategory upsertExpenditureCategory(ExpenditureCategory expenditureCategory) {
        ExpenditureCategory savedExpenditureCategory = expenditureCategoryRepo.save(expenditureCategory);
        if (savedExpenditureCategory == null) {
            logger.error("Service: ExpenditureCategory could not be saved");
            throw new RuntimeException("Saving expenditureCategory failed");
        }
        return savedExpenditureCategory;
    }

    /**
     * Find all expenditureCategories or one expenditureCategory by id
     * @param id The id of the expenditureCategory to be found (READ)
     * @return List of expenditureCategories
     */
    public List<ExpenditureCategory> findExpenditureCategory(Long id) {
        List<ExpenditureCategory> expenditureCategoryList = new ArrayList<>();
        if (id == null) {
            expenditureCategoryRepo.findAll().forEach(expenditureCategoryList::add);
            logger.info("Service: Get all expenditureCategories as list.");
        } else {
            expenditureCategoryRepo.findById(id).ifPresent(expenditureCategoryList::add);
            logger.info("Service: Get expenditureCategory by id " + id);
        }
        return expenditureCategoryList;
    }

    /**
     * Delete all expenditureCategories or an expentitureCategory by id
     * @param id The id of the expenditureCategory to be deleted (DELETE)
     * @return List of deleted expenditureCategories
     */
    public List<ExpenditureCategory> deleteExpenditureCategory(Long id) {
        List<ExpenditureCategory> expenditureCategoryList = new ArrayList<>();
        if (id == null) {
            expenditureCategoryRepo.findAll().forEach(expenditureCategoryList::add);
            expenditureCategoryRepo.deleteAll();
            logger.warn("Service: Delete all expenditureCategories.");
        } else {
            expenditureCategoryRepo.findById(id).ifPresent(expenditureCategoryList::add);
            List<Expenditure> expenditures = expenditureService.findExpenditure(null);
            for (Expenditure expenditure : expenditures) {
                if (expenditure.getExpenditureCategory() == expenditureCategoryList.getFirst()) {
                    expenditure.setExpenditureCategory(null);
                    expenditureRepo.save(expenditure);
                }
            }
            expenditureCategoryRepo.deleteById(id);
            logger.info("Service: Delete expenditureCategory by id " + id);
        }
        return expenditureCategoryList;
    }

    // Use-Case-Operations for the entity account

    /**
     * Returns the cost of the given category by id for the current month.
     * @param id The id of the expenditureCategory (READ)
     * @return cost as String
     */
    public String getCurrentMonth(Long id) {
        List<ExpenditureCategory> expenditureCategoryList = new ArrayList<>();
        Date now = new Date();
        int currentYear = now.getYear();
        int currentMonth = now.getMonth();
        BigDecimal myCost = new BigDecimal(0);
        if (id == null) {
            logger.error("Service: Can't get cost of current month by expenditureCategory null");
        } else {
            expenditureCategoryRepo.findById(id).ifPresent(expenditureCategoryList::add);
            for (Expenditure expenditure : expenditureCategoryList.getFirst().getExpenditures()) {
                Date date = expenditure.getDate();
                if (date.getYear() == currentYear && date.getMonth() == currentMonth) {
                    myCost = myCost.add(expenditure.getAmount());
                }
            }
        }
        logger.info("Service: Get the cost of current month by expenditureCategory " + id);
        return myCost.toString();
    }
}
