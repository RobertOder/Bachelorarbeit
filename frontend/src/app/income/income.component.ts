import { Component, Input } from '@angular/core';
import { CommonModule, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Account } from '../model/account';
import { Income } from '../model/income';
import { HouseholdMember } from '../model/householdMember';
import { AccountService } from '../service/account.service';
import { HouseholdMemberService } from '../service/householdMember.service';
import { Router } from '@angular/router';
import { Household } from '../model/household';

@Component({
  selector: 'app-income',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './income.component.html',
  styleUrl: './income.component.css'
})
export class IncomeComponent {

  @Input() householdMember!: HouseholdMember;
  @Input() income!: Income;
  // myVar: any;
  accounts: Account[] = [];
  households: Household[] = [];

  constructor(private householdMemberService: HouseholdMemberService, private accountService: AccountService, private router: Router) {
    this.householdMember = history.state;
  }

  ngOnInit(): void {
    this.income = new Income();
    this.getAccounts();
    if (this.income.date) {
      this.income.dateString = this.income.date.toString().split('T')[0];
    } else {
      this.income.dateString = new Date().toISOString().split('T')[0];
    }
  }

  getAccounts(): void {
    this.householdMemberService.getAccounts(this.householdMember.id)
    .subscribe(accounts => this.accounts = accounts);
  }

  getHouseholds(): void {
    this.householdMemberService.getHouseholds(this.householdMember.id)
    .subscribe(households => this.households = households);
  }

  add(income: Income, accountId: string): void {
    income.currency = 'EUR';
    income.recurring = false;
    income.date = new Date(this.income.dateString);
    this.accountService.addIncome(Number(accountId), income).subscribe(tmp => {
      this.router.navigate(['home-component']);
    });
  }

}
