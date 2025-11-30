import { Component, Input } from '@angular/core';
import { CommonModule, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Household } from '../model/household';
import { HouseholdMember } from '../model/householdMember';
import { ExpenditureCategory } from '../model/expenditureCategory';
import { HouseholdService } from '../service/household.service';
import { HouseholdMemberService } from '../service/householdMember.service';
import { ExpenditureCategoryService } from '../service/expenditureCategory.service';

@Component({
  selector: 'app-expenditureCategory',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './expenditureCategory.component.html',
  styleUrl: './expenditureCategory.component.css'
})
export class ExpenditureCategoryComponent {

  @Input() householdMember!: HouseholdMember;
  @Input() expenditureCategory!: ExpenditureCategory;
  // myVar: any;
  households: Household[] = [];
  currentExpendituresSum: { [key: number]: string } = {};

  constructor(private householdMemberService: HouseholdMemberService, private householdService: HouseholdService, private expenditureCategoryService: ExpenditureCategoryService) { 
    this.householdMember = history.state;
  }
  
  ngOnInit(): void {
    this.expenditureCategory = new ExpenditureCategory();
    this.getHouseholdsAndCurrentExpendituresSum();
  }

  getHouseholdsAndCurrentExpendituresSum(): void {
    this.householdMemberService.getHouseholds(this.householdMember.id)
    .subscribe(households => {
      this.households = households;
      this.households.forEach(household => {
        household.expenditureCategories.forEach(category => {
          this.expenditureCategoryService.getExpenditureSum(category.id).subscribe(amount => {
            this.currentExpendituresSum[category.id] = amount;
          });
        });
      });
    });
  }

  add(expenditureCategory: ExpenditureCategory, householdId: string): void {
    const { expenditures, ...toSavedExpenditureCategory } = expenditureCategory;
    this.householdService.addExpenditureCategory(Number(householdId), toSavedExpenditureCategory).subscribe(tmp => {
      this.ngOnInit();
    });
  }

  deleteCategory(expenditureCategory: ExpenditureCategory): void {
    this.expenditureCategoryService.deleteExpenditureCategory(expenditureCategory.id).subscribe( tmp => {
      this.ngOnInit();
    });
  }

  editExpenditureCategory(expenditureCategory: ExpenditureCategory): void {
      this.expenditureCategory = expenditureCategory;
    }

  getCurrentMonth(categoryId: number): string {
    return this.currentExpendituresSum[categoryId] !== undefined
      ? this.currentExpendituresSum[categoryId]
      : 'Lädt...';
  }

}