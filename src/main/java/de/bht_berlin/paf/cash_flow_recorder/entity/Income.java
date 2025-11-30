package de.bht_berlin.paf.cash_flow_recorder.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

/**
 * Class to represent an income
 */
@NoArgsConstructor
@Getter
@Setter
@Entity // Spring JPA annotation:DB
public class Income {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date date;
    private BigDecimal amount;
    private Currency currency;
    private Boolean recurring;
    private Enum recurringInterval;
    private String description;
    @ManyToOne
    @JoinColumn(name = "account_id")
    @JsonBackReference("account-incomes")
    private Account account;

    /**
     * Constructor
     * @param date date of the income transfer
     * @param amount amount of the income
     * @param currency currency of the income
     * @param recurring income is recurring
     * @param recurringInterval the interval of the recurring
     * @param description description of the income
     */
    public Income(Date date, BigDecimal amount, Currency currency, Boolean recurring, Enum recurringInterval, String description) {
        this.date = date;
        this.amount = amount;
        this.currency = currency;
        this.recurring = recurring;
        this.recurringInterval = recurringInterval;
        this.description = description;
    }
}
