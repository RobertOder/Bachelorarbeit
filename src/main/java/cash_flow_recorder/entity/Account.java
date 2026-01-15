package cash_flow_recorder.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

/**
 * Class to represent an account
 */
@NoArgsConstructor
@Getter
@Setter
@Entity // Spring JPA annotation: DB
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // unique id
    private String name; // name if the account
    private BigDecimal balance; // balance of the account
    private Currency currency; // currency
    @ManyToOne
    @JoinColumn(name = "household_member_id")
    @JsonBackReference("householdMember-accounts")
    private HouseholdMember householdMember;
    @ManyToOne
    @JoinColumn(name = "household_id")
    @JsonBackReference("household-accounts")
    private Household household;
    @OneToMany(mappedBy = "account", cascade = CascadeType.REMOVE)
    @JsonManagedReference("account-expenditures")
    private List<Expenditure> expenditures = new ArrayList<>();
    @OneToMany(mappedBy = "account", cascade = CascadeType.REMOVE)
    @JsonManagedReference("account-incomes")
    private List<Income> incomes = new ArrayList<>();


    /**
     * Constructor
     * @param name name of the account
     * @param balance balance of the account
     * @param currency currency of the current Country
     */
    public Account(String name, BigDecimal balance, Currency currency) {
        this.name = name;
        this.balance = balance;
        this.currency = currency;
    }
}
