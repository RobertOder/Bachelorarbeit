package de.bht_berlin.paf.cash_flow_recorder.service;

import de.bht_berlin.paf.cash_flow_recorder.entity.ExpenditureCategory;
import de.bht_berlin.paf.cash_flow_recorder.entity.Household;
import de.bht_berlin.paf.cash_flow_recorder.entity.HouseholdMember;
import de.bht_berlin.paf.cash_flow_recorder.repo.ExpenditureCategoryRepo;
import de.bht_berlin.paf.cash_flow_recorder.repo.HouseholdMemberRepo;
import de.bht_berlin.paf.cash_flow_recorder.repo.HouseholdRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service-Class for the entity household
 */
@Service
public class HouseholdService {

    private final HouseholdRepo householdRepo;
    private final ExpenditureCategoryRepo expenditureCategoryRepo;
    private final HouseholdMemberRepo householdMemberRepo;
    private final HouseholdMemberService householdMemberService;
    private final Logger logger = LoggerFactory.getLogger(HouseholdService.class);

    @Autowired // Constructor Injection
    public HouseholdService(HouseholdRepo householdRepo, ExpenditureCategoryRepo expenditureCategoryRepo, HouseholdMemberRepo householdMemberRepo, HouseholdMemberService householdMemberService) {
        this.householdRepo = householdRepo;
        this.expenditureCategoryRepo = expenditureCategoryRepo;
        this.householdMemberRepo = householdMemberRepo;
        this.householdMemberService = householdMemberService;
    }

    // CRUD-Operations for the entity householdMember
    // Upsert household over the householdMember

    /**
     * Save or update a household (upsert = update + insert)
     * @param household Household to be saved (CREATE / UPDATE)
     * @return Created or updated household
     */
    public Household upsertHousehold(Household household) {
        Household savedHousehold = householdRepo.save(household);
        if (savedHousehold == null) {
            logger.error("Service: Failed to save household");
            throw new RuntimeException("Saving Household failed");
        }
        logger.info("Service: Saved household with id " + savedHousehold.getId());
        return savedHousehold;
    }

    /**
     * Find all households or a household by id
     * @param id The id of the household to be found (READ)
     * @return List of households
     */
    public List<Household> findHousehold(Long id) {
        List<Household> householdList = new ArrayList<>();
        if (id == null) {
            householdRepo.findAll().forEach(householdList::add);
            logger.info("Service: Get all households as list");
        } else {
            householdRepo.findById(id).ifPresent(householdList::add);
            logger.info("Service: Get household with id " + id);
        }
        return householdList;
    }

    /**
     * Delete all households or a household by id
     * @param id The id of the household to be deleted (DELETE)
     * @return List of deleted households
     */
    public List<Household> deleteHousehold(Long id) {
        List<Household> householdList = new ArrayList<>();
        if (id == null) {
            householdRepo.findAll().forEach(householdList::add);
            householdRepo.deleteAll();
            logger.warn("Service: Delete all households");
        } else {
            householdRepo.findById(id).ifPresent(householdList::add);
            householdList.getFirst().setCreator(null);
            householdRepo.save(householdList.getFirst());
            List<HouseholdMember> householdMemberList = householdMemberService.findHouseholdMember(null);
            for (HouseholdMember householdMember : householdMemberList) {
                householdMember.getHouseholds().remove(householdList.getFirst());
                householdMemberRepo.save(householdMember);
            }
            householdRepo.deleteById(id);
            logger.info("Service: Delete household with id " + id);
        }
        return householdList;
    }

    // Use-Case-Operations for the entity account

    /**
     * Added an ExpenditureCategory for a Household
     * @param id The id of the household to be added the expenditureCategory
     * @param expenditureCategory The expenditureCategory to be saved to the household
     * @return updated household
     */
    public Household addExpenditureCategory(Long id, ExpenditureCategory expenditureCategory) {
        if (id == null || expenditureCategory == null) {
            logger.error("Service: Saving expenditureCategory failed, not all required parameters set");
            throw new RuntimeException("Saving ExpenditureCategory failed, not all required parameters set");
        }
        Household household = householdRepo.findById(id).orElse(null);
        if (household == null) {
            logger.error("Service: Saving expenditureCategory failed, no such household with id " + id);
            throw new RuntimeException("Saving expenditureCategory failed, no such household with id " + id);
        }
        expenditureCategory.setHousehold(household);
        ExpenditureCategory savedExpenditureCategory = expenditureCategoryRepo.save(expenditureCategory);
        if (savedExpenditureCategory == null) {
            logger.error("Service: Saving expenditureCategory failed");
            throw new RuntimeException("Saving expenditureCategory failed");
        }
        logger.info("Service: Saved expenditureCategory with id " + savedExpenditureCategory.getId());
        return household;
    }

    /**
     * Get all saved expenditureCategories from a household by id
     * @param id The id from the household
     * @return A list with all expenditureCategories of the household
     */
    public List<ExpenditureCategory> getExpenditureCategories(Long id) {
        Household household = householdRepo.findById(id).orElse(null);
        if (household == null) {
            logger.error("Service: Cant get expenditureCategories, no such household with id " + id);
            throw new RuntimeException("Cant get expenditureCategories, no such household with id: " + id);
        }
        logger.info("Service: Get expenditureCategories with household id " + id);
        return household.getExpenditureCategories();
    }
}
