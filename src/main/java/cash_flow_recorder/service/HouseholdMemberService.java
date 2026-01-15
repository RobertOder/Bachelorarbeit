package cash_flow_recorder.service;

import cash_flow_recorder.entity.Account;
import cash_flow_recorder.entity.Household;
import cash_flow_recorder.entity.HouseholdMember;
import cash_flow_recorder.repo.AccountRepo;
import cash_flow_recorder.repo.HouseholdMemberRepo;
import cash_flow_recorder.repo.HouseholdRepo;
import cash_flow_recorder.repo.IncomeRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service-Class for the entity householdMember
 */
@Service
public class HouseholdMemberService {

    private final HouseholdMemberRepo householdMemberRepo;
    private final HouseholdRepo householdRepo;
    private final AccountRepo accountRepo;
    private final Logger logger = LoggerFactory.getLogger(HouseholdMemberService.class);

    @Autowired // Constructor Injection
    public HouseholdMemberService(HouseholdMemberRepo householdMemberRepo, HouseholdRepo householdRepo, AccountRepo accountRepo, IncomeRepo incomeRepo) {
        this.householdMemberRepo = householdMemberRepo;
        this.householdRepo = householdRepo;
        this.accountRepo = accountRepo;
    }

    // CRUD-Operations for the entity householdMember

    /**
     * Save or update a householdMember (upsert = update + insert)
     * @param householdMember HouseholdMember to be saved (CREATE / UPDATE)
     * @return Created or updated householdMember
     */
    public HouseholdMember upsertHouseholdMember(HouseholdMember householdMember) {
        if (householdMember.getId() != null) {
            logger.warn("Service: Saving HouseholdMember, id of householdMember not set");
            HouseholdMember foundHouseholdMember = householdMemberRepo.findById(householdMember.getId()).orElse(null);
            if (foundHouseholdMember != null) {
                householdMember.setHouseholds(foundHouseholdMember.getHouseholds());
            }
        }
        HouseholdMember savedHouseholdMember = householdMemberRepo.save(householdMember);
        if (savedHouseholdMember == null) {
            logger.error("Service: Saving HouseholdMember failed");
            throw new RuntimeException("Saiving Householdmember failed");
        }
        logger.info("Service: Saving householdMember successful with id " + savedHouseholdMember.getId());
        return savedHouseholdMember;
    }

    /**
     * Find all householdMembers or a householdMember by id
     * @param id The id of the householdMember to be found (READ)
     * @return List of householdMembers
     */
    public List<HouseholdMember> findHouseholdMember(Long id) {
        List<HouseholdMember> householdMemberList = new ArrayList<>();
        if (id == null) {
            householdMemberRepo.findAll().forEach(householdMemberList::add);
            logger.info("Service: Get all householdMember");
        } else {
            householdMemberRepo.findById(id).ifPresent(householdMemberList::add);
            logger.info("Service: Get householdMember by id " + id);
        }
        return householdMemberList;
    }

    /**
     * Delete a householdMember by id
     * @param id The id of the householdMember to be deleted (DELETE)
     * @return List of deleted householdMembers
     */
    public List<HouseholdMember> deleteHouseholdMember(Long id) {
        if (id == null) {
            logger.error("Service: Delete householdMember failed, id of householdMember not set");
            throw new RuntimeException("Delete Householdmember failed, no id given.");
        }
        List<HouseholdMember> householdMemberList = new ArrayList<>();
        householdMemberRepo.findById(id).ifPresent(householdMemberList::add);
        for (Household household : householdMemberList.getFirst().getHouseholds()) {
            if (household.getCreator() != null && household.getCreator().getId().equals(id)) {
                household.setCreator(null);
                householdRepo.save(household);
            }
        }
        householdMemberList.getFirst().getHouseholds().clear();
        householdMemberRepo.save(householdMemberList.getFirst());
        householdMemberRepo.deleteById(id);
        logger.info("Service: Delete householdMember successful with id " + id);
        return householdMemberList;
    }

    // Use-Case-Operations for the entity householdMember

    /**
     * Added an household for a householdMember
     * @param id The id from the householdMember to be added the household
     * @param household The household to be saved to the householdMember
     * @return updated householdMember
     */
    public HouseholdMember addHousehold(Long id, Household household) {
        if (id == null || household == null) {
            logger.error("Service: Saiving Household failed, no given id for a householdMember.");
            throw new RuntimeException("Saiving Household failed, no given id for a householdMember.");
        }
        HouseholdMember householdMember = householdMemberRepo.findById(id).orElse(null);
        if (householdMember == null) {
            logger.error("Service: Saving household failed, no such householdMember with id: " + id);
            throw new RuntimeException("Saving household failed, no such householdMember with id: " + id);
        }
        Household foundedHousehold = null;
        if (household.getId() != null) {
            foundedHousehold = householdRepo.findById(household.getId()).orElse(null);
            logger.info("Service: Get household by id " + household.getId() + " for update.");
        }
        Household savedHousehold = null;
        if (foundedHousehold == null) {
            household.setCreator(householdMember);
            savedHousehold = householdRepo.save(household);
            logger.info("Service: Saving new household by householdMember with id " + householdMember.getId());
        }else{
            savedHousehold = householdRepo.save(foundedHousehold);
        }
        if (savedHousehold == null) {
            logger.error("Service: Saving household failed");
            throw new RuntimeException("Saving household failed");
        }
        householdMember.getHouseholds().add(savedHousehold);
        HouseholdMember savedHouseholdMember = householdMemberRepo.save(householdMember);
        if (savedHouseholdMember == null) {
            logger.error("Service: Saving householdMember failed");
            throw new RuntimeException("Saving householdMember failed");
        }
        logger.info("Service: Saving household successful by householdMember id " + savedHouseholdMember.getId());
        return savedHouseholdMember;
    }

    /**
     * Added an account for a householdMember
     * @param id The id from the householdMember to be added the account
     * @param householdId The id from the household to be added the account
     * @param account The account to be saved to the householdMember
     * @return updated householdMember
     */
    public HouseholdMember addAccount(Long id, Long householdId, Account account) {
        if (id == null || householdId == null || account == null) {
            logger.error("Service: Saiving account failed, not all required parameters are set");
            throw new RuntimeException("Saving account failed, not all required parameters are set");
        }
        HouseholdMember householdMember = householdMemberRepo.findById(id).orElse(null);
        Household household = householdRepo.findById(householdId).orElse(null);
        if (householdMember == null || household == null) {
            logger.error("Service: Saving account failed, no such householdMember with id: " + id + " or household with id: " + householdId);
            throw new RuntimeException("Service: Saving account failed, no such householdMember with id: " + id + " or household with id: " + householdId);
        }
        account.setHousehold(household);
        account.setHouseholdMember(householdMember);
        Account savedAccount = accountRepo.save(account);
        if (savedAccount == null) {
            logger.error("Service: Saving account failed");
            throw new RuntimeException("Saving account failed");
        }
        HouseholdMember savedHouseholdMember = householdMemberRepo.save(householdMember);
        household.getAccounts().add(savedAccount);
        Household savedHousehold = householdRepo.save(household);
        if (savedHouseholdMember == null || savedHousehold == null) {
            logger.error("Service: Saving account for householdMember or household failed");
            throw new RuntimeException("Saving account for householdMember or household failed");
        }
        logger.info("Service: Saving account successful with id " + savedAccount.getId());
        return savedHouseholdMember;
    }

    /**
     * Get all saved households to a given householdMember by id
     * @param id The id from the householdMember
     * @return A list with all households from the householdMember
     */
    public List<Household> getHouseholds(Long id) {
        HouseholdMember householdMember = householdMemberRepo.findById(id).orElse(null);
        if (householdMember == null) {
            logger.error("Service: No households, with such householdMember id: " + id);
            throw new RuntimeException("No households, with such householdMember id: " + id);
        }
        logger.info("Service: Get households successful by householdMember id " + id);
        return householdMember.getHouseholds();
    }

    /**
     * Get all saved accounts to a given householdMember by id
     * @param id The id from the householdMember
     * @return A list with all accounts from the householdMember
     */
    public List<Account> getAccounts(Long id) {
        HouseholdMember householdMember = householdMemberRepo.findById(id).orElse(null);
        if (householdMember == null) {
            logger.error("Service: No accounts, with such householdMember id: " + id);
            throw new RuntimeException("No accounts, with such householdMember id: " + id);
        }
        logger.info("Service: Get accounts successful by householdMember id " + id);
        return householdMember.getAccounts();
    }
}
