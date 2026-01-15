package cash_flow_recorder.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent a category for expenditures
 */
@NoArgsConstructor
@Getter
@Setter
@Entity // Spring JPA annotation: DB
public class ExpenditureCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private BigDecimal upperLimit;
    @ManyToOne
    @JoinColumn(name = "household_id")
    @JsonBackReference("household-expenditureCategory")
    private Household household;
    @OneToMany(mappedBy = "expenditureCategory")
    @JsonManagedReference("expenditure-expenditureCategory")
    private List<Expenditure> expenditures = new ArrayList<>();

    /**
     * Constructor
     * @param name name of the category for expenditures
     * @param upperLimit upper limit of the category for expenditures
     */
    public ExpenditureCategory(String name, BigDecimal upperLimit) {
        this.name = name;
        this.upperLimit = upperLimit;
    }
}
