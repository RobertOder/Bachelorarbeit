package cash_flow_recorder.service;

import cash_flow_recorder.entity.Account;
import cash_flow_recorder.entity.Expenditure;
import cash_flow_recorder.entity.Income;
import cash_flow_recorder.repo.AccountRepo;
import cash_flow_recorder.repo.ExpenditureRepo;
import cash_flow_recorder.repo.IncomeRepo;
import cash_flow_recorder.repo.ReceiptCopyRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service-Class for the entity account
 */
@Service
public class AccountService {

    private final AccountRepo accountRepo;
    private final IncomeRepo incomeRepo;
    private final ExpenditureRepo expenditureRepo;
    private final Logger logger = LoggerFactory.getLogger(AccountService.class);

    @Autowired // Constructor Injection
    public AccountService(AccountRepo accountRepo, IncomeRepo incomeRepo, ExpenditureRepo expenditureRepo, ReceiptCopyRepo receiptCopyRepo) {
        this.accountRepo = accountRepo;
        this.incomeRepo = incomeRepo;
        this.expenditureRepo = expenditureRepo;
    }

    // CRUD-Operations for the entity householdMember
    // Upsert account over the householdMember

    /**
     * Save or update an account (upsert = update + insert)
     * @param account Account to be saved (CREATE / UPDATE)
     * @return Created or updated account
     */
    public Account upsertAccount(Account account){
        Account savedAccount = accountRepo.save(account);
        if(savedAccount == null){
            logger.error("Service: Account could not be saved");
            throw new RuntimeException("Saving account failed");
        }
        logger.info("Service: Account saved with id " + savedAccount.getId());
        return savedAccount;
    }

    /**
     * Find all accounts or an account by id
     * @param id The id of the account to be found (READ)
     * @return List of accounts
     */
    public List<Account> findAccount(Long id) {
        List<Account> accountList = new ArrayList<>();
        if (id == null) {
            accountRepo.findAll().forEach(accountList::add);
            logger.info("Service: Get all accounts as list.");
        } else {
            accountRepo.findById(id).ifPresent(accountList::add);
            logger.info("Service: Have to find account with id " + id);
        }
        return accountList;
    }

    /**
     * Delete all accounts or an account by id
     * @param id The id of the account to be deleted (DELETE)
     * @return List of deleted accounts
     */
    public List<Account> deleteAccount(Long id) {
        List<Account> accountList = new ArrayList<>();
        if (id == null) {
            accountRepo.findAll().forEach(accountList::add);
            accountRepo.deleteAll();
            logger.warn("Service: Delete all accounts.");
        } else {
            accountRepo.findById(id).ifPresent(accountList::add);
            accountRepo.deleteById(id);
            logger.info("Service: Delete account with id " + id);
        }
        return accountList;
    }

    // Use-Case-Operations for the entity account

    /**
     * Added an income for an account
     * @param id The id from the account to be added the income
     * @param income The income to be saved to the account
     * @return Updated account
     */
    public Income addIncome(Long id, Income income) {
        if (id == null || income == null) {
            logger.error("Service: Saving income failed, not all required parameters set");
            throw new RuntimeException("Saving income failed, not all required parameters set");
        }
        Account account = accountRepo.findById(id).orElse(null);
        if (account == null) {
            logger.error("Service: Saving income failed, no such account with id " + id);
            throw new RuntimeException("Saving income failed, no such account with id " + id);
        }
        Income savedIncome = null;
        income.setAccount(account);
        savedIncome = incomeRepo.save(income);
        if (savedIncome == null) {
            logger.error("Service: Income for account could not be saved");
            throw new RuntimeException("Saving income for account failed");
        }
        account.getIncomes().add(savedIncome);
        account.setBalance(account.getBalance().add(income.getAmount()));
        Account savedAccount = accountRepo.save(account);
        if(savedAccount == null){
            logger.error("Service: Account could not be saved");
            throw new RuntimeException("Saving account failed");
        }
        logger.info("Service: Income saved with id " + savedAccount.getId());
        return savedIncome;
    }

    /**
     * Added an expenditure for an account
     * @param id The id from the account to be added the expenditure
     * @param expenditure The expenditure to be saved to the account
     * @return Updated account
     */
    public Expenditure addExpenditure(Long id, Expenditure expenditure) {
        if (id == null || expenditure == null) {
            logger.error("Service: Saving expenditure failed, not all required parameters set");
            throw new RuntimeException("Saving expenditure failed, not all required parameters set");
        }
        Account account = accountRepo.findById(id).orElse(null);
        if (account == null) {
            logger.error("Service: Saving expenditure failed, no such account with id " + id);
            throw new RuntimeException("Saving expenditure failed, no such account with id " + id);
        }
        Expenditure savedExpenditure = null;
        expenditure.setAccount(account);
        savedExpenditure = expenditureRepo.save(expenditure);
        if (savedExpenditure == null) {
            logger.error("Service: Expenditure for account could not be saved");
            throw new RuntimeException("Saving expenditure failed");
        }
        account.getExpenditures().add(savedExpenditure);
        account.setBalance(account.getBalance().subtract(expenditure.getAmount()));
        Account savedAccount = accountRepo.save(account);
        if(savedAccount == null){
            logger.error("Service: Account could not be saved");
            throw new RuntimeException("Saving expenditure for account failed");
        }
        logger.info("Service: Expenditure saved with id " + savedAccount.getId());
        return savedExpenditure;
    }
}
