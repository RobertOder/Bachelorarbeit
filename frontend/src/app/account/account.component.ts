import { Component, Input } from '@angular/core';
import { CommonModule, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Account } from '../model/account';
import { Household } from '../model/household';
import { HouseholdMember } from '../model/householdMember';
import { HouseholdMemberService } from '../service/householdMember.service';
import { AccountService } from '../service/account.service';

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './account.component.html',
  styleUrl: './account.component.css'
})
export class AccountComponent {

  @Input() householdMember!: HouseholdMember;
  @Input() account!: Account;
  households: Household[] = [];

  constructor(private householdMemberService: HouseholdMemberService, private accountService: AccountService) { 
    this.householdMember = history.state;
  }
  
  ngOnInit(): void {
    this.account = new Account();
    this.getHouseholds();
  }

  getHouseholds(): void {
    this.householdMemberService.getHouseholds(this.householdMember.id)
    .subscribe(households => this.households = households);
  }

  add(account: Account, householdId: string): void {
    account.currency = 'EUR';
    const { expenditures, incomes, ...toSavedAccount } = account;
    this.householdMemberService.addAccount(this.householdMember.id, Number(householdId), toSavedAccount).subscribe(tmp => {
      this.householdMember = tmp;
      this.ngOnInit();
    });
  }

  deleteAccount(account: Account): void {
    this.accountService.deleteAccount(account.id).subscribe(tmp => {
      this.ngOnInit();
    });
  }

  editAccount(account: Account): void {
    this.account = account;
  }

}