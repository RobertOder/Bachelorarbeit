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
  currentExpendituresSum: { [key: number]: string[] } = {};
  diagramColors = [
    'red',
    'green',
    'yellow',
    'blue',
    'fuchsia',
    'aqua',
    'white',
    'orange',
    'purple',
    'teal'
  ];
  actualYear: number = new Date().getFullYear();
  actualMonth: number = new Date().getMonth() +1;
  monthYear: string = this.actualMonth.toString().padStart(2, '0') + " / " + this.actualYear;

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
            let amountAndColor: string[] = [];
            amountAndColor.push(amount);
            amountAndColor.push(this.diagramColors[category.id % this.diagramColors.length]);
            this.currentExpendituresSum[category.id] = amountAndColor;
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
    return this.currentExpendituresSum[categoryId]?.[0] !== undefined
      ? this.currentExpendituresSum[categoryId][0]
      : 'Lädt...';
  }

  getDiagramBackground(expenditureCategories: ExpenditureCategory[]): string {
    let total = 0;
    for (const expCat of expenditureCategories) {
      if (this.currentExpendituresSum[expCat.id]) { // Erst wenn OnInit durch ist
        total += Number(this.currentExpendituresSum[expCat.id][0]);
      }
    }
    console.log("total: " + total);
    let calc = 0;
    const parts: string[] = [];
    for (let i = 0; i < expenditureCategories.length; i++) {
      const expCat = expenditureCategories[i];
      if (this.currentExpendituresSum[expCat.id]) { // Erst wenn OnInit durch ist
        const start = (calc / total) * 100;
        calc += Number(this.currentExpendituresSum[expCat.id][0]);
        const end = (calc / total) * 100;

        const color = this.currentExpendituresSum[expCat.id][1];

        parts.push(`${color} ${start}% ${end}%`);
      }
    }

    return `conic-gradient(${parts.join(', ')})`;
  }

  getLegendColor(expCat: ExpenditureCategory): string {
    let color = "black";
    if (this.currentExpendituresSum[expCat.id]) { // Erst wenn OnInit durch ist
      color = this.currentExpendituresSum[expCat.id][1];
    }
    return color;
  }

  protected readonly Number = Number;
}
