package cash_flow_recorder.service;

import cash_flow_recorder.entity.*;
import cash_flow_recorder.entity.*;
import cash_flow_recorder.repo.ExpenditureRepo;
import cash_flow_recorder.repo.ExpenditureCategoryRepo;
import cash_flow_recorder.repo.ReceiptCopyRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * Service-Class for the entity expenditure
 */
@Service
public class ExpenditureService {

    private final ExpenditureRepo expenditureRepo;
    private final ExpenditureCategoryRepo expenditureCategoryRepo;
    private final ReceiptCopyRepo receiptCopyRepo;
    private final HouseholdMemberService householdMemberService;
    private final Logger logger = LoggerFactory.getLogger(ExpenditureService.class);

    @Autowired // Contructor Injection
    public ExpenditureService(ExpenditureRepo expenditureRepo, ExpenditureCategoryRepo expenditureCategory, ReceiptCopyRepo receiptCopyRepo, HouseholdService householdService, HouseholdMemberService householdMemberService) {
        this.expenditureRepo = expenditureRepo;
        this.expenditureCategoryRepo = expenditureCategory;
        this.receiptCopyRepo = receiptCopyRepo;
        this.householdMemberService = householdMemberService;
    }

    // CRUD-Operations for the entity expenditure
    // Creation over the entity account

    /**
     * Save or update an expenditure (upsert = update + insert)
     * @param expenditure Expenditure to be saved (CREATE / UPDATE)
     * @return Created or updated expenditure
     */
    public Expenditure upsertExpenditure(Expenditure expenditure){
        Expenditure savedExpenditure = expenditureRepo.save(expenditure);
        if(savedExpenditure == null){
            logger.error("Service: Expenditure could not be saved");
            throw new RuntimeException("Saving expenditure failed");
        }
        return savedExpenditure;
    }

    /**
     * Find all expenditures or one expenditure by id
     * @param id The id of the expenditure to be found (READ)
     * @return List of expenditures
     */
    public List<Expenditure> findExpenditure(Long id) {
        List<Expenditure> expenditureList = new ArrayList<>();
        if (id == null) {
            expenditureRepo.findAll().forEach(expenditureList::add);
            logger.info("Service: Get all expenditures as list");
        } else {
            expenditureRepo.findById(id).ifPresent(expenditureList::add);
            logger.info("Service: Get expenditure by id " + id);
        }
        return expenditureList;
    }

    /**
     * Delete all expenditures or an expenditure by id
     * @param id The id of the expenditure to be deleted (DELETE)
     * @return List of deleted expenditures
     */
    public List<Expenditure> deleteExpenditure(Long id) {
        List<Expenditure> expenditureList = new ArrayList<>();
        if (id == null) {
            expenditureRepo.findAll().forEach(expenditureList::add);
            expenditureRepo.deleteAll();
            logger.warn("Service: Delete all expenditures.");

        } else {
            expenditureRepo.findById(id).ifPresent(expenditureList::add);
            expenditureRepo.deleteById(id);
            logger.info("Service: Delete expenditure by id " + id);
        }
        return expenditureList;
    }

    // Use-Case-Operations for the entity expenditure

    /**
     * Added an receiptCopy for an expenditure
     * @param id The id from the expenditure to be added the receiptCopy
     * @param photoFile The file of the receiptCopy to be saved to the expenditure
     * @return Saved expenditure
     */
    public Expenditure addReceiptCopy(Long id, MultipartFile photoFile) {
        if (id == null || photoFile == null || photoFile.isEmpty()) {
            logger.error("Service: Saving receiptCopy failed, not all required parameters are set");
            throw new RuntimeException("Saving receiptCopy failed, not all required parameters are set");
        }
        Expenditure expenditure = expenditureRepo.findById(id).orElse(null);
        if (expenditure == null) {
            logger.error("Service: Saving receiptCopy failed, no such expenditure with id " + id);
            throw new RuntimeException("Saving receiptCopy failed, no such expenditure with id " + id);
        }
        String fileName = photoFile.getOriginalFilename();
        try {
            photoFile.transferTo(new java.io.File(System.getProperty("user.dir") + "/uploads/" + new Date().getTime()/1000 + fileName));
        } catch (Exception e) {
            logger.error("Service: Saving image for receiptCopy failed: " + e.getMessage());
            e.printStackTrace();
        }
        ReceiptCopy newReceiptCopy = new ReceiptCopy();
        newReceiptCopy.setDate(new Date());
        newReceiptCopy.setExpenditure(expenditure);
        newReceiptCopy.setPhotoPath("/uploads/" + new Date().getTime()/1000 + fileName);
        // ToDo Translation over OCR
        // newReceiptCopy.setTranslation("in progress...");

        ReceiptCopy savedReceiptCopy = receiptCopyRepo.save(newReceiptCopy);
        if(savedReceiptCopy == null){
            logger.error("Service: Saving receiptCopy for expenditure failed");
            throw new RuntimeException("Saving receiptCopy for expenditure failed");
        }
        expenditure.getReceiptCopies().add(savedReceiptCopy);
        Expenditure savedExpenditure = expenditureRepo.save(expenditure);
        if(savedExpenditure == null){
            logger.error("Service: Saving expenditure for receiptCopy failed");
            throw new RuntimeException("Saving expenditure for receiptCopy failed");
        }
        logger.info("Service: Add receiptCopy");
        return savedExpenditure;
    }

    /**
     * Assign an expenditure to a expenditureCategory by IDs
     * @param id The id from the expenditure to be assigned to the expenditureCategory
     * @param categoryId The id from the expenditureCategory
     * @return Saved expenditure
     */
    public Expenditure assignCategory(Long id, Long categoryId) {
        Expenditure expenditure = expenditureRepo.findById(id).orElse(null);
        if (expenditure == null) {
            logger.error("Service: Assign expenditureCategory to expenditure failed, no such expenditure with id " + id);
            throw new RuntimeException("Assign expenditureCategory to expenditure failed, no such expenditure with id " + id);
        }
        ExpenditureCategory foundedexpenditureCategory = expenditureCategoryRepo.findById(categoryId).orElse(null);
        if (foundedexpenditureCategory == null) {
            logger.error("Service: Assign expenditureCategory to expenditure failed, no such expenditureCategory with id " + categoryId);
            throw new RuntimeException("Assign expenditureCategory to expenditure failed, no such expenditureCategory with id " + categoryId);
        }
        expenditure.setExpenditureCategory(foundedexpenditureCategory);
        Expenditure savedExpenditure = expenditureRepo.save(expenditure);
        if (savedExpenditure == null) {
            logger.error("Service: Assign expenditureCategory for expenditure failed");
            throw new RuntimeException("Assign expenditureCategory for expenditure failed");
        }
        logger.info("Service: Assign expenditure to expenditureCategory");
        return savedExpenditure;
    }

    /**
     * Find all expenditures by householdMember id
     * @param id The id from the householdMember
     * @return A List with all expenditures from the householdMember
     */
    public List<Expenditure> findExpenditureByHouseholdMember(Long id) {
        List<Expenditure> expenditureList = new ArrayList<>();
        if (id == null) {
            logger.error("Service: Find expenditure by householdMember id " + id + " failed, no such id");
            throw new RuntimeException("Find householdMember failed, not all required parameters are set");
        }
        List<HouseholdMember> selectedHouseholdMember = householdMemberService.findHouseholdMember(id);
        List<Account> householdMemberAccounts = selectedHouseholdMember.getFirst().getAccounts();
        for ( Account account : householdMemberAccounts ) {
            for (Expenditure expenditure : account.getExpenditures()) {
                expenditureList.add(expenditure);
            }
        }
        logger.info("Service: Find expenditure by householdMember id " + id);
        return expenditureList;
    }

    /**
     * Returns the account from which the expense was deducted.
     * @param id The id of the expenditure
     * @return The account of the expenditure
     */
    public Account getAccount(Long id) {
        if (id == null) {
            logger.error("Service: Get account failed, not all required parameters are set");
            throw new RuntimeException("Get account failed, not all required parameters are set");
        }
        Expenditure expenditure = expenditureRepo.findById(id).orElse(null);
        if (expenditure == null) {
            logger.error("Service: Find account failed, no such expenditure with id " + id);
            throw new RuntimeException("Find account failed, no such expenditure with id " + id);
        }
        logger.info("Service: Get account for expenditure with id " + id);
        return expenditure.getAccount();
    }

    /**
     * Returns the expenditureCategory from given expenditure by id
     * @param id The id of the expenditure
     * @return The expenditureCategory of the expenditure
     */
    public ExpenditureCategory getExpenditureCategory(Long id) {
        ExpenditureCategory expenditureCategory = new ExpenditureCategory();
        if (id == null) {
            logger.error("Service: Get expenditureCategory by expenditure id " + id + " failed, no such id");
            throw new RuntimeException("Find expenditureCategory failed, not all required parameters are set");
        }
        Expenditure expenditure = expenditureRepo.findById(id).orElse(null);
        if (expenditure == null) {
            logger.error("Service: Find expenditureCategory failed, no such expenditure with id " + id);
            throw new RuntimeException("Find expenditureCategory failed, no such expenditure with id " + id);
        }
        if (expenditure.getExpenditureCategory() != null) {
            expenditureCategory = expenditure.getExpenditureCategory();
        }
        logger.info("Service: Get expenditureCategory for expenditure with id " + id);
        return expenditureCategory;
    }

    /**
     * Returns the receiptCopies from given expenditure by id
     * @param id The id of the expenditure
     * @return A List of receiptCopies
     */
    public List<ReceiptCopy> getReceiptCopies(Long id) {
        if (id == null) {
            logger.error("Service: Get receiptCopy failed, not all required parameters are set");
            throw new RuntimeException("Find receiptCopies failed, not all required parameters are set");
        }
        Expenditure expenditure = expenditureRepo.findById(id).orElse(null);
        if (expenditure == null) {
            logger.error("Get receiptCopies failed, no such expenditure with id " + id);
            throw new RuntimeException("Get receiptCopies failed, no such expenditure with id " + id);
        }
        logger.info("Service: Get receiptCopy for expenditure with id " + id);
        return expenditure.getReceiptCopies();
    }
}
