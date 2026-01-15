package cash_flow_recorder.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class to represent a householdmember
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
public class HouseholdMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String firstname;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String password;
    private Date created;
    @OneToMany(cascade =  CascadeType.ALL, mappedBy = "householdMember")
    @JsonManagedReference("householdMember-accounts")
    private List<Account> accounts = new ArrayList<>();
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "household_member_join_houshold",
            joinColumns = @JoinColumn(name = "household_member_id"),
            inverseJoinColumns = @JoinColumn(name = "household_id")
    )
    @JsonManagedReference("householdMember-households")
    private List<Household> households = new ArrayList<>();

    /**
     * Constructor
     * @param name name of the householdmember
     * @param firstname firstname of the householdmember
     * @param email email-address of the householdmember
     * @param phone phonenumber of the householdmember
     * @param address address of the householdmember
     * @param city city of the householdmember
     * @param state state of the householdmember
     * @param zip zip of the householdmember
     * @param country country of the householdmember
     * @param password password of the householdmember
     * @param created Date of creation of the householdmember
     */
    public HouseholdMember(String name, String firstname, String email, String phone, String address, String city, String state, String zip, String country, String password, Date created) {
        this.name = name;
        this.firstname = firstname;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.country = country;
        this.password = password;
        this.created = created;
    }

}
