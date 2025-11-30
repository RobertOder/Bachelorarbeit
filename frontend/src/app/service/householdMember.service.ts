
import { Injectable, NgModule } from '@angular/core';
import { HttpClient, HttpClientModule, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { HouseholdMember } from '../model/householdMember';
import { Household } from '../model/household';
import { Account } from '../model/account';
import { BrowserModule } from '@angular/platform-browser';

// @NgModule({
//     imports: [
//     BrowserModule,
//     HttpClientModule,
//     ],
// })
@Injectable({ providedIn: 'root' })
export class HouseholdMemberService {

  private householdMemberUrl = 'http://localhost:8080/api/householdMember';  // URL to web api

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(private http: HttpClient) { }

  addHouseholdMember(householdMember: HouseholdMember): Observable<HouseholdMember> {
    const url = `${this.householdMemberUrl}/upsert`;
    householdMember.created = new Date();
    return this.http.patch<HouseholdMember>(url, householdMember, this.httpOptions).pipe(
      tap((newHouseholdMember: HouseholdMember) => this.log('added householdMember')),
      catchError(this.handleError<HouseholdMember>('addHouseholdMember'))
    );
  }

  addHousehold(householdMember: HouseholdMember, household: Household): Observable<HouseholdMember> {
    const url = `${this.householdMemberUrl}/${householdMember.id}/addHousehold`;
    return this.http.patch<HouseholdMember>(url, household, this.httpOptions).pipe(
      tap((newHouseholdMember: HouseholdMember) => this.log('added household')),
      catchError(this.handleError<HouseholdMember>('addHousehold'))
    );
  }

  getHouseholdMembers(): Observable<HouseholdMember[]> {
    return this.http.get<HouseholdMember[]>(this.householdMemberUrl)
    .pipe(
        tap(_ => this.log('fetched householdMembers')),
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

  deleteHouseholdMember(id: number): Observable<string> {
    const url = `${this.householdMemberUrl}/${id}`;
    return this.http.delete(url, { responseType: 'text' }).pipe(
      tap(response  => this.log(`Server response: ${response}`)),
      catchError(this.handleError<string>(`deleteHouseholdMember id=${id}`))
    );
  }

  getHouseholds(id: number): Observable<Household[]> {
    const url = `${this.householdMemberUrl}/${id}/households`;
    return this.http.get<Household[]>(url).pipe(
      tap(_ => this.log(`get households by member id=${id}`)),
      catchError(this.handleError<Household[]>(`getHouseholds id=${id}`))
    );
  }

  addAccount(id: number, householdId: number, account: Account): Observable<HouseholdMember> {
    const url = `${this.householdMemberUrl}/${id}/addAccount?household=${householdId}`;
    console.log(url);
    return this.http.patch<HouseholdMember>(url, account, this.httpOptions).pipe(
      tap((householdMember: HouseholdMember) => this.log('added Account')),
      catchError(this.handleError<HouseholdMember>('addAccount'))
    );
  }

  getAccounts(id: number): Observable<Account[]> {
    const url = `${this.householdMemberUrl}/${id}/accounts`;
    return this.http.get<Account[]>(url).pipe(
      tap(_ => this.log(`get accounts by member id=${id}`)),
      catchError(this.handleError<Account[]>(`getAccounts id=${id}`))
    );
  }


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
      console.log(`HouseholdMemberService: ${message}`);
  }
}