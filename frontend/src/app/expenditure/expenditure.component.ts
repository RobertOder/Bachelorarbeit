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
import {Observable} from 'rxjs';

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
  selectedFileView: string | null = null;
  selectedAccountId: number | null = null;
  selectedExpenditureCategoryId: number | null = null;
  receiptImageUrls: { [id: number]: string } = {}; // Speichert die Bild-URLs mit ID als Schluessel
  loadingTranslate = false;
  loadingCategory = false;

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
        this.selectedFileView = this.expenditureService.getRecognizedDocument();
      } else {
        console.log("Konnte history.state nicht verarbeiten")
      }
    } catch (error) {
      console.error('Fehler beim Verarbeiten von history.state:', error);
    }
  }

  async ngOnInit(): Promise<void> {
    // Hier pruefen, ob File bereits gesetzt wurde....
    this.getAccounts();
    if (this.expenditure?.receiptCopies) {
      this.expenditure.receiptCopies.forEach(receiptCopy => {
        this.getImage(receiptCopy.id);
      });
    }
    if (this.selectedFileView) {
      const dataURL: string = this.selectedFileView;
      this.selectedFile = await this.dataURLtoFile(dataURL, "recognizedDocument.png");
    }
    if (this.expenditure.date) {
      this.expenditure.dateString = this.expenditure.date.toString().split('T')[0];
    } else {
      this.expenditure.dateString = new Date().toISOString().split('T')[0];
    }
  }

  async dataURLtoFile(dataUrl: string, filename: string): Promise<File> {
    const res =  await fetch(dataUrl);
    const blob =  await res.blob();
    return new File([blob], filename,{ type: 'image/png' })
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      this.selectedFileView = URL.createObjectURL(file);
    }
  }

  getAccounts(): void {
    this.householdMemberService.getAccounts(this.householdMember.id)
    .subscribe(accounts => {
      this.accounts = accounts;
      if (this.expenditure.account?.id != null) {
        this.selectedAccountId = this.expenditure.account.id;
        this.updateExpenditureCategories();
      }
    });
  }

  validate(): boolean {
    let accountId = document.getElementById("account") as HTMLSelectElement;
    let parent = accountId?.parentNode;
    if(!accountId.value){
      if (parent?.lastChild != null) {
        if (parent.lastChild.nodeName.toLowerCase() !== "div") {
          // Element erzeugen
          let div = document.createElement("div");
          // Die Attribute class und id setzen
          div.setAttribute("class", "message");
          div.setAttribute("id", accountId.id + "_err");
          // Element mit Inhalt (Text) befuellen
          let message = document.createTextNode("Bitte Konto wählen");
          // Kindelement platzieren
          div.appendChild(message);
          // Fertiges Element in den DOM-Baum einfuegen
          parent.appendChild(div);
        }
      }
      return false;
    }
    else{
      return true;
    }
  }

  add(event: Event): void {
    if (!this.validate()) {
      // Neuladen der Seite verhindern
      event.preventDefault();
    } else {
      var toSavedExpenditure: Expenditure = {} as Expenditure;
      var accountId = document.getElementById("account") as HTMLSelectElement;
      var expenditureCategoryId = document.getElementById("expenditureCategory") as HTMLSelectElement;

      toSavedExpenditure.id = this.expenditure.id;
      toSavedExpenditure.description = this.expenditure.description;
      toSavedExpenditure.date = new Date(this.expenditure.dateString);
      toSavedExpenditure.amount = this.expenditure.amount;
      this.expenditure.currency = 'EUR';
      this.expenditure.recurring = false;
      this.expenditureService.removeRecognizedDocument();
      this.accountService.addExpenditure(Number(accountId.value), toSavedExpenditure, this.selectedFile).subscribe(expenditure => {
        this.expenditureService.assignCategory(expenditure.id, Number(expenditureCategoryId.value)).subscribe(tmp => {
          this.router.navigate(['home-component']);
        });
      });
    }
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
    this.loadingTranslate = true;
    this.receiptCopyService.translateReceiptCopy(receiptCopy.id).subscribe({
      next: (tmp) => {
        const textfield = document.getElementById('ocr-textfield-' + receiptCopy.id) as HTMLTextAreaElement;
        if (textfield) {
          textfield.value = tmp;
        }
        this.router.navigate(['home-component']);
      }, complete: () => {
        this.loadingTranslate = false;
      }
    });
  }

  categorizeReceiptCopy(receiptCopy: ReceiptCopy): void {
    this.loadingCategory = true;
    var accountId = document.getElementById("account") as HTMLSelectElement;
    var searchedAccountId = Number(accountId.value);
    if (this.selectedAccountId) {
      searchedAccountId = this.selectedAccountId;
    }
    this.accountService.getHousehold(searchedAccountId)
      .subscribe(household => {
        this.receiptCopyService.categorizeReceiptCopy(receiptCopy.id, household.id).subscribe({
          next: (tmp) => {
            const textfield = document.getElementById('categorie-textfield-' + receiptCopy.id) as HTMLTextAreaElement;
            if (textfield) {
              textfield.value = tmp;
            }
            //this.router.navigate(['home-component']);
          }, complete: () => {
            this.loadingCategory = false;
          }
        });
      });
  }

  documentRecognizer(householdMember: HouseholdMember, expenditure: Expenditure): void {
    this.router.navigate( ['documentRecognizer'], { state: {
        householdMember,
        expenditure
      }
    });
  }

  removeDocument(): void {
    this.selectedFileView = null;
    this.expenditureService.removeRecognizedDocument();
  }

}
