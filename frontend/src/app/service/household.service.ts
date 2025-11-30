
import { Injectable, NgModule } from '@angular/core';
import { HttpClient, HttpClientModule, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { Household } from '../model/household';
import { ExpenditureCategory } from '../model/expenditureCategory';
import { BrowserModule } from '@angular/platform-browser';

// @NgModule({
//     imports: [
//     BrowserModule,
//     HttpClientModule,
//     ],
// })
@Injectable({ providedIn: 'root' })
export class HouseholdService {

  private householdUrl = 'http://localhost:8080/api/household';  // URL to web api

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(private http: HttpClient) { }

  getHouseholds(): Observable<Household[]> {
    return this.http.get<Household[]>(this.householdUrl)
    .pipe(
        tap(_ => this.log('fetched households')),
        catchError(this.handleError<Household[]>('getHouseholds', []))
      );
  }

  getHousehold(id: number): Observable<Household> {
    const url = `${this.householdUrl}/${id}`;
        return this.http.get<Household>(url).pipe(
          tap(_ => this.log(`fetched household id=${id}`)),
          catchError(this.handleError<Household>(`getHousehold id=${id}`))
        );
  }

  deleteHousehold(id: number): Observable<string> {
      const url = `${this.householdUrl}/${id}`;
      return this.http.delete(url, { responseType: 'text'}).pipe(
        tap(response => this.log(`Server response: ${response}`)),
        catchError(this.handleError<string>(`deleteHousehold id=${id}`))
      );
  }

  addExpenditureCategory(id: number, expenditureCategory: ExpenditureCategory): Observable<ExpenditureCategory> {
    const url = `${this.householdUrl}/${id}/addExpenditureCategory`;
    console.log(url);
    return this.http.patch<ExpenditureCategory>(url, expenditureCategory, this.httpOptions).pipe(
      tap((newExpenditureCategory: ExpenditureCategory) => this.log('added ExpenditureCategory')),
      catchError(this.handleError<ExpenditureCategory>('addExpenditureCategory'))
    );
  }

  getExpenditureCategories(id: number): Observable<ExpenditureCategory[]> {
    const url = `${this.householdUrl}/${id}/expenditureCategories`;
    return this.http.get<ExpenditureCategory[]>(url).pipe(
      tap(_ => this.log(`get expenditureCategory by household id=${id}`)),
      catchError(this.handleError<ExpenditureCategory[]>(`getExpenditureCategories id=${id}`))
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
      console.log(`HouseholdService: ${message}`);
  }

}