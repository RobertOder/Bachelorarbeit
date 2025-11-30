import { Component, Input } from '@angular/core';
import { CommonModule, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Account } from '../model/account';
import { HouseholdMember } from '../model/householdMember';
import { AccountService } from '../service/account.service';
import { HouseholdMemberService } from '../service/householdMember.service';
import { HouseholdService } from '../service/household.service';
import { ExpenditureService } from '../service/expenditure.service';
import { ReceiptCopyService } from '../service/receiptCopy.service';
import { Expenditure } from '../model/expenditure';
import { ReceiptCopy } from '../model/receiptCopy';
import { ExpenditureCategory } from '../model/expenditureCategory';
import { Router } from '@angular/router';

@Component({
  selector: 'app-expenditure',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './expenditure.component.html',
  styleUrl: './expenditure.component.css'
})
export class ExpenditureComponent {

  @Input() householdMember!: HouseholdMember;
  @Input() expenditure!: Expenditure;

  // myVar: any;
  accounts: Account[] = [];
  expenditureCategories: ExpenditureCategory[] = [];
  selectedFile: File | null = null;
  selectedAccountId: number | null = null;
  selectedExpenditureCategoryId: number | null = null;
  receiptImageUrls: { [id: number]: string } = {}; // Speichert die Bild-URLs mit ID als Schlüssel

  constructor(private householdMemberService: HouseholdMemberService,
              private accountService: AccountService,
              private householdService: HouseholdService,
              private expenditureService: ExpenditureService,
              private receiptCopyService: ReceiptCopyService,
              private router: Router) {
    try{
      if (history.state && history.state.expenditure && history.state.householdMember) {
        const { expenditure, householdMember } = history.state;
        this.householdMember = householdMember;
        if (expenditure) {
          this.expenditure = expenditure;
        }
      } else {
        console.log("Konnte history.state nicht verarbeiten")
      }
    } catch (error) {
      console.error('Fehler beim Verarbeiten von history.state:', error);
    }
  }

  ngOnInit(): void {
    this.getAccounts();
    if (this.expenditure?.receiptCopies) {
      this.expenditure.receiptCopies.forEach(receiptCopy => {
        this.getImage(receiptCopy.id);
      });
    }
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  getAccounts(): void {
    this.householdMemberService.getAccounts(this.householdMember.id)
    .subscribe(accounts => {
      this.accounts = accounts;
      if (this.expenditure.account.id != null) {
        this.selectedAccountId = this.expenditure.account.id;
        this.updateExpenditureCategories();
      }
    });
  }

  add(): void {
    var toSavedExpenditure: Expenditure = {} as Expenditure;
    var accountId = document.getElementById("account") as HTMLSelectElement;
    var expenditureCategoryId = document.getElementById("expenditureCategory") as HTMLSelectElement;

    toSavedExpenditure.id = this.expenditure.id;
    toSavedExpenditure.description = this.expenditure.description;
    if (this.expenditure.date == null) {
      toSavedExpenditure.date = new Date();
    }else{
      toSavedExpenditure.date = this.expenditure.date;
    }
    toSavedExpenditure.amount = this.expenditure.amount;
    this.expenditure.currency = 'EUR';
    this.expenditure.recurring = false;
    this.accountService.addExpenditure(Number(accountId.value), toSavedExpenditure, this.selectedFile).subscribe(expenditure => {
      this.expenditureService.assignCategory(expenditure.id , Number(expenditureCategoryId.value)).subscribe( tmp => {
        this.router.navigate(['home-component']);
      });
    });
  }

  updateExpenditureCategories(): void {
    var accountId = document.getElementById("account") as HTMLSelectElement;
    var searchedAccountId = Number(accountId.value);
    if (this.selectedAccountId) {
      searchedAccountId = this.selectedAccountId;
    }
    this.accountService.getHousehold(searchedAccountId)
    .subscribe(household => {
      this.householdService.getExpenditureCategories(household.id)
        .subscribe(expenditureCategories => {
          this.expenditureCategories = expenditureCategories;
          if (this.expenditure.expenditureCategory) {
            this.selectedExpenditureCategoryId = this.expenditure.expenditureCategory.id;;
          }
        });
    });
  }

  deleteReceiptCopy(receiptCopy: ReceiptCopy): void {
    this.expenditure.receiptCopies = this.expenditure.receiptCopies.filter(tmpReceiptCopy => tmpReceiptCopy !== receiptCopy);
    this.receiptCopyService.deleteReceiptCopy(receiptCopy.id).subscribe();
  }

  getImage(id: number): void {
    this.receiptCopyService.getReceiptCopyImage(id).subscribe(
      (blob) => {
        const objectURL = URL.createObjectURL(blob);
        this.receiptImageUrls[id] = objectURL;
      },
      (error) => console.error('Error loading image:', error)
    );
  }

  translateReceiptCopy(receiptCopy: ReceiptCopy): void {
    this.receiptCopyService.translateReceiptCopy(receiptCopy.id).subscribe( tmp => {
      const textfield = document.getElementById('textfield-' + receiptCopy.id) as HTMLTextAreaElement;
      if (textfield) {
        textfield.value = tmp;
      }
      this.router.navigate(['home-component']);
  });
  }

}
