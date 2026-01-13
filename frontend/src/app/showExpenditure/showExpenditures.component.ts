import { Component, OnInit, Input } from '@angular/core';
import { HouseholdMember } from '../model/householdMember';
import { ExpenditureService } from '../service/expenditure.service';
import { CommonModule } from '@angular/common';
import { NgFor, NgForOf } from "@angular/common";
import { Router } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { Expenditure } from '../model/expenditure';
import { Account } from '../model/account';

@Component({
  selector: 'app-showExpenditures',
  standalone: true,
  imports: [CommonModule, NgFor],
  templateUrl: './showExpenditures.component.html',
  styleUrl: './showExpenditures.component.css'
})
export class ShowExpendituresComponent {

  @Input() householdMember!: HouseholdMember;
  expenditures: Expenditure[] = [];

  constructor(private expenditureService: ExpenditureService, private router: Router) {
    try{
      if (history.state && history.state.expenditure && history.state.householdMember) {
        console.log(`HALLOE`);
        const { expenditure, householdMember } = history.state;
        this.householdMember = householdMember;
        console.log(`Mal gucken... ${this.householdMember.address}`);
      } else {
        console.log("Konnte history.state nicht verarbeiten")
      }
    } catch (error) {
      console.error('Fehler beim Verarbeiten von history.state:', error);
    }
  }

  ngOnInit(): void {
    this.getExpenditures();
  }

  getExpenditures(): void {
    this.expenditureService.getExpendituresFromHouseholdMember(this.householdMember.id)
    .subscribe(expenditures => {
      this.expenditures = expenditures;
      this.expenditures.forEach((expenditure) => {
        this.expenditureService.getAccount(expenditure.id).subscribe(account => {
          expenditure.account = account;
        });
        this.expenditureService.getExpenditureCategory(expenditure.id).subscribe(category => {
          expenditure.expenditureCategory = category;
        });
      });
    });
  }

  delete(expenditure: Expenditure): void {
    this.expenditures = this.expenditures.filter(tmpExpenditure => tmpExpenditure !== expenditure);
    this.expenditureService.deleteExpenditure(expenditure.id).subscribe();
  }

  updateExpenditure(expenditure: Expenditure): void {
    this.router.navigate(['expenditure-component'], {
      state: {
        householdMember: this.householdMember,
        expenditure
      }
    });
  }

}
