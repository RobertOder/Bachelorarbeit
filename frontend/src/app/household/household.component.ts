import { Component, Input } from '@angular/core';
import { CommonModule, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HouseholdMember } from '../model/householdMember';
import { Household } from '../model/household';
import { HouseholdMemberService } from '../service/householdMember.service';
import { HouseholdService } from '../service/household.service';

@Component({
  selector: 'app-household',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './household.component.html',
  styleUrl: './household.component.css'
})
export class HouseholdComponent {

  @Input() householdMember!: HouseholdMember;
  @Input() household!: Household;
  // Array for selection to enter a household
  households: Household[] = [];
  // myVar: any;

  constructor(private householdMemberService: HouseholdMemberService, private householdService: HouseholdService) { 
    this.householdMember = history.state;
  }
  
  ngOnInit(): void {
    this.household = new Household();
    this.getHouseholds();
  }

  getHouseholds(): void {
    this.householdService.getHouseholds()
    .subscribe(households => this.households = households);
  }

  addNewHousehold(household: Household): void {
    household.created = new Date();
    this.householdMemberService.addHousehold(this.householdMember, household).subscribe(tmp => {
      this.getHouseholds();
      this.householdMember = tmp;
    });
  }

  add(householdId: string): void {
    this.households.forEach(household => {
        if (household.id == Number(householdId)) {
            if (!this.householdMember.households?.some(h => h.id == household.id)){
              const { creator, ...toSavedHousehold } = household;
              this.householdMemberService.addHousehold(this.householdMember, toSavedHousehold).subscribe(tmp =>{
                this.ngOnInit();
                this.householdMember = tmp;
              });
            }
        }
    });
  }

  deleteHousehold(household: Household): void {
    this.householdMember.households = this.householdMember.households?.filter(tmpHousehold => tmpHousehold !== household);
    this.householdService.deleteHousehold(household.id).subscribe();
  }

}