import { Routes } from '@angular/router';
import { HouseholdMemberComponent } from './householdMember/householdMember.component';
import { HouseholdComponent } from './household/household.component';
import { AccountComponent } from './account/account.component';
import { IncomeComponent } from './income/income.component';
import { ExpenditureComponent } from './expenditure/expenditure.component';
import { ExpenditureCategoryComponent } from './expenditureCategory/expenditureCategory.component';
import { ShowExpendituresComponent } from './showExpenditure/showExpenditures.component';
import { AppComponent } from './app.component';
import { HomeComponent } from './home/home.component';
import { DocumentRecognizerComponent } from './documentRecognizer/documentRecognizer.component';

export const routes: Routes = [
    // see index.html for start web page

    { path: 'home-component', component: HomeComponent },
    { path: 'householdMember-component', component: HouseholdMemberComponent },
    { path: 'household-component', component: HouseholdComponent },
    { path: 'account-component', component: AccountComponent },
    { path: 'income-component', component: IncomeComponent},
    { path: 'expenditure-component', component: ExpenditureComponent},
    { path: 'expenditureCategory-component', component: ExpenditureCategoryComponent},
    { path: 'showExpenditures-component', component: ShowExpendituresComponent},
    { path: 'documentRecognizer', component: DocumentRecognizerComponent}

];
