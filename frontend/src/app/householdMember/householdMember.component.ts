import { Component, Input } from '@angular/core';
import { CommonModule, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HouseholdMember } from '../model/householdMember';
import { HouseholdMemberService } from '../service/householdMember.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-householdMember',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './householdMember.component.html',
  styleUrl: './householdMember.component.css'
})
export class HouseholdMemberComponent {

  @Input() householdMember!: HouseholdMember;

  constructor(private householdMemberService: HouseholdMemberService,  private router: Router) { 
    this.householdMember = history.state;
  }
  
  ngOnInit(): void {
  }

  add(householdMember: HouseholdMember): void {
    const { households, accounts, ...toSavedHouseholdMember } = householdMember;
    this.householdMemberService.addHouseholdMember(toSavedHouseholdMember).subscribe(tmp => {
      this.router.navigate(['home-component']);
    });
  }

  delete(householdMember: HouseholdMember): void {
    this.householdMemberService.deleteHouseholdMember(householdMember.id).subscribe(tmp => {
      this.router.navigate(['home-component']);
    });
  }

}