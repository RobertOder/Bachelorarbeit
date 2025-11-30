import { Component, OnInit } from '@angular/core';
import { HouseholdMember } from '../model/householdMember';
import { Expenditure } from '../model/expenditure';
import { HouseholdMemberService } from '../service/householdMember.service';
import { CommonModule } from '@angular/common';
import { NgFor, NgForOf } from "@angular/common";
import { Router } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, NgFor],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {

  title = "Aktionsmenü für Mitglieder";
  householdMembers: HouseholdMember[] = [];

  constructor(private householdMemberService: HouseholdMemberService, private router: Router) { }

  ngOnInit(): void {
    this.getHouseholdMembers();
  }

  getHouseholdMembers(): void {
    this.householdMemberService.getHouseholdMembers()
    .subscribe(householdMembers => this.householdMembers = householdMembers);
  }

  getHouseholdMember(householdMember: HouseholdMember): void {
    this.householdMemberService.getHouseholdMember(householdMember.id);
  }

  updateHouseholdMember(householdMember: HouseholdMember): void {
    this.router.navigate(['householdMember-component'], { state: householdMember});
  }

  addHousehold(householdMember: HouseholdMember): void {
    this.router.navigate(['household-component'], { state: householdMember});
  }

  addAccount(householdMember: HouseholdMember): void {
    this.router.navigate( ['account-component'], { state: householdMember });
  }

  addIncome(householdMember: HouseholdMember): void {
    this.router.navigate( ['income-component'], { state: householdMember });
  }

  addExpenditure(householdMember: HouseholdMember): void {
    this.router.navigate( ['expenditure-component'], { 
      state: {
        householdMember,
        expenditure: new Expenditure()
      }
     });
  }

  addExpenditureCategory(householdMember: HouseholdMember): void {
    this.router.navigate( ['expenditureCategory-component'], { state: householdMember });
  }

  showExpenditures(householdMember: HouseholdMember): void {
    this.router.navigate( ['showExpenditures-component'], { 
      state: {
        householdMember,
        expenditure: new Expenditure()
      }
    });
  }

  getTitle(): string {
    return this.title;
  }

}