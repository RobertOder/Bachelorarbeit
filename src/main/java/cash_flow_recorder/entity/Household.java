package cash_flow_recorder.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class to represent a household
 */
@NoArgsConstructor
@Getter
@Setter
@Entity // Spring JPA annotation: DB
public class Household {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // unique id
    private String name; // name of the household
    @ManyToOne
    @JoinColumn(name = "creator_id")
    @JsonBackReference("householdMember-households")
    private HouseholdMember creator; // HouseholdMember there created
    private Date created; // Timestamp of the creation
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "household")
    @JsonManagedReference("household-expenditureCategory")
    private List<ExpenditureCategory> expenditureCategories = new ArrayList<>();
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "household")
    @JsonManagedReference("household-accounts")
    private List<Account> accounts = new ArrayList<>();
    //@ManyToMany(mappedBy = "households")
    //@JsonBackReference("householdMember-households")
    //private List<HouseholdMember> householdMembers = new ArrayList<>();

    /**
     * Constructor
     * @param name name of the household
     * @param created Date of the Creation
     */
    public Household(String name, Date created) {
        this.name = name;
        this.created = created;
    }
}
