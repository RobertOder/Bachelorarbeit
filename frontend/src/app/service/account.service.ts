
import { Injectable, NgModule } from '@angular/core';
import { HttpClient, HttpClientModule, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { Account } from '../model/account';
import { Income } from '../model/income';
import { Household } from '../model/household';
import { Expenditure } from '../model/expenditure';
import { ExpenditureService } from './expenditure.service';
import { BrowserModule } from '@angular/platform-browser';

// @NgModule({
//     imports: [
//     BrowserModule,
//     HttpClientModule,
//     ],
// })
@Injectable({ providedIn: 'root' })
export class AccountService {

  private accountUrl = 'http://localhost:8080/api/account';  // URL to web api

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(private http: HttpClient, private expenditureService: ExpenditureService) { }

  getAccount(id: number): Observable<Account> {
    const url = `${this.accountUrl}/${id}`;
        return this.http.get<Account>(url).pipe(
          tap(_ => this.log(`fetched Account id=${id}`)),
          catchError(this.handleError<Account>(`getAccount id=${id}`))
        );
  }

  addIncome(id: number, income: Income): Observable<Income> {
    const url = `${this.accountUrl}/${id}/addIncome`;
    console.log(url);
    return this.http.patch<Income>(url, income, this.httpOptions).pipe(
      tap((newIncome: Income) => this.log('added Income')),
      catchError(this.handleError<Income>('addIncome'))
    );
  }

  addExpenditure(id: number, expenditure: Expenditure, photoFile: File | null): Observable<Expenditure> {
    const url = `${this.accountUrl}/${id}/addExpenditure`;
    console.log(url);
    return this.http.patch<Expenditure>(url, expenditure, this.httpOptions).pipe(
      map((response: Expenditure) => {
        // Add receiptCopy
        if (photoFile) {
          this.expenditureService.addReceiptCopy(response.id, photoFile).subscribe();
        }
        console.log('Response processed:', response);
        return response;
    }),
      tap((newExpenditure: Expenditure) => this.log('added Expenditure')),
      catchError(this.handleError<Expenditure>('addExpenditure'))
    );
  }

  getHousehold(id: number): Observable<Household> {
    const url = `${this.accountUrl}/${id}/household`;
        return this.http.get<Household>(url).pipe(
          tap(_ => this.log(`fetched household over account-id=${id}`)),
          catchError(this.handleError<Household>(`getHousehold over account-id=${id}`))
        );
  }

  deleteAccount(id: number): Observable<string> {
    const url = `${this.accountUrl}/${id}`;
    return this.http.delete(url, { responseType: 'text' }).pipe(
      tap(response => this.log(`Server response: ${response}`)),
      catchError(this.handleError<string>(`deleteAccount id=${id}`))
    );
}

  // ToDo only one use-case "addExpenditureCategory"

  /**
  addExpenditureCategory(householdMember: HouseholdMember): Observable<HouseholdMember> {
    const url = `${this.householdMemberUrl}/upsert`;
    householdMember.created = new Date();
    return this.http.patch<HouseholdMember>(url, householdMember, this.httpOptions).pipe(
      tap((newHouseholdMember: HouseholdMember) => this.log('added householdMember')),
      catchError(this.handleError<HouseholdMember>('addHouseholdMember'))
    );
  }

  getHouseholdMembers(): Observable<HouseholdMember[]> {
    return this.http.get<HouseholdMember[]>(this.householdMemberUrl)
    .pipe(
        tap(_ => this.log('fetched pictures')),
        catchError(this.handleError<HouseholdMember[]>('getHouseholdMembers', []))
      );
  }


  getHouseholdMember(id: number): Observable<HouseholdMember> {
    const url = `${this.householdMemberUrl}/${id}`;
    return this.http.get<HouseholdMember>(url).pipe(
      tap(_ => this.log(`fetched householdMember id=${id}`)),
      catchError(this.handleError<HouseholdMember>(`getHouseholdMember id=${id}`))
    );
  }

  deleteHouseholdMember(id: number): Observable<HouseholdMember> {
    const url = `${this.householdMemberUrl}/${id}`;
    return this.http.delete<HouseholdMember>(url).pipe(
      tap(_ => this.log(`deleted householdMember id=${id}`)),
      catchError(this.handleError<HouseholdMember>(`deleteHouseholdMember id=${id}`))
    );
  }

*/

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

        // TODO: send the error to remote logging infrastructure
        console.error(error); // log to console instead

        // TODO: better job of transforming error for user consumption
        this.log(`${operation} failed: ${error.message}`);

        // Let the app keep running by returning an empty result.
        return of(result as T);
    };
  }

  private log(message: string) {
      // this.messageService.add(`PictureService: ${message}`);
      console.log(`AccountService: ${message}`);
  }

}