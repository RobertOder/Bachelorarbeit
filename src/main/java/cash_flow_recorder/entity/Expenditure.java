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
import java.util.Date;
import java.util.List;

/**
 * Class to represent an expenditure
 */
@NoArgsConstructor
@Getter
@Setter
@Entity // Spring JPA annotation:DB
public class Expenditure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date date;
    private BigDecimal amount;
    private Currency currency;
    private Boolean recurring;
    private Enum recurringInterval;
    private String description;
    @ElementCollection
    private List<String> article = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "account_id")
    @JsonBackReference("account-expenditures")
    private Account account;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "expenditure")
    @JsonManagedReference("Expenditure-ReceiptCopies")
    private List<ReceiptCopy> receiptCopies = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "expenditure_category_id")
    @JsonBackReference("expenditure-expenditureCategory")
    private ExpenditureCategory expenditureCategory;

    /**
     * Constructor
     * @param date date of the expenditure transfer
     * @param amount amount of the expenditure
     * @param currency currency of the expenditure
     * @param recurring expenditure is recurring
     * @param recurringInterval the interval of the recurring
     * @param description description of the expenditure
     */
    public Expenditure(Date date, BigDecimal amount, Currency currency, Boolean recurring, Enum recurringInterval, String description, List<String> article) {
        this.date = date;
        this.amount = amount;
        this.currency = currency;
        this.recurring = recurring;
        this.recurringInterval = recurringInterval;
        this.description = description;
        this.article = article;
    }
}
