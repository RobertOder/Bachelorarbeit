
import { Injectable, NgModule } from '@angular/core';
import { HttpClient, HttpClientModule, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { Expenditure } from '../model/expenditure';
import { BrowserModule } from '@angular/platform-browser';
import { Account } from '../model/account';
import { ExpenditureCategory } from '../model/expenditureCategory';

// @NgModule({
//     imports: [
//     BrowserModule,
//     HttpClientModule,
//     ],
// })
@Injectable({ providedIn: 'root' })
export class ExpenditureService {

  // Hier noch File definieren? Fuer den documentRecognizer?
  private expenditureUrl = 'http://localhost:8080/api/expenditure';  // URL to web api
  private recognizedDocument: string | null = null;

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(private http: HttpClient) { }

  addReceiptCopy(id: number, photoFile: File): Observable<any> {
    const url = `${this.expenditureUrl}/${id}/addReceiptCopy`;
    const formData = new FormData();
    formData.append('photoFile', photoFile);
    // Send POST-Request
    return this.http.patch(url, formData).pipe(
      tap((response) => this.log('added ReceiptCopy')),
      catchError(this.handleError<Expenditure>('addReceiptCopy'))
    );
  }

  getExpenditures(id: number): Observable<Expenditure[]> {
    return this.http.get<Expenditure[]>(this.expenditureUrl)
    .pipe(
        tap(_ => this.log('getExpenditures: fetched expenditures')),
        catchError(this.handleError<Expenditure[]>('getExpenditures', []))
      );
  }

  getExpendituresFromHouseholdMember(id: number): Observable<Expenditure[]> {
    const url = `${this.expenditureUrl}?householdMember=${id}`;
    return this.http.get<Expenditure[]>(url)
    .pipe(
        tap(_ => this.log('getExpendituresFromHouseholdMember: fetched expenditures')),
        catchError(this.handleError<Expenditure[]>('getExpendituresFromHouseholdMember', []))
      );
  }

  assignCategory(id: number, expenditureCategoryId: number): Observable<Expenditure> {
    const url = `${this.expenditureUrl}/${id}/assignCategory?expenditureCategory=${expenditureCategoryId}`;
    return this.http.patch<Expenditure>(url, null, this.httpOptions).pipe(
        tap((newExpenditure: Expenditure) => this.log('assign expenditure to category')),
        catchError(this.handleError<Expenditure>('assign expenditure to category'))
      );
  }

  deleteExpenditure(id: number): Observable<string> {
    const url = `${this.expenditureUrl}/${id}`;
    return this.http.delete(url, { responseType: 'text' }).pipe(
      tap(response => this.log(`Server response: ${response}`)),
      catchError(this.handleError<string>(`deleteExpenditure id=${id}`))
    );
  }

  getAccount(id: number): Observable<Account> {
    const url = `${this.expenditureUrl}/${id}/account`;
    return this.http.get<Account>(url)
    .pipe(
        tap(_ => this.log('getAccount: fetched account')),
        catchError(this.handleError<Account>('getAccount'))
      );
  }

  getExpenditureCategory(id: number): Observable<ExpenditureCategory> {
    const url = `${this.expenditureUrl}/${id}/expenditureCategory`;
    return this.http.get<ExpenditureCategory>(url)
    .pipe(
        tap(_ => this.log('getExpenditureCategory: fetched expenditureCategory')),
        catchError(this.handleError<ExpenditureCategory>('getExpenditureCategory'))
      );
  }

  setRecognizedDocument(dataUrl: string | null) {
    this.recognizedDocument = dataUrl;
  }

  getRecognizedDocument(): string | null {
    return this.recognizedDocument;
  }

  removeRecognizedDocument() {
    this.recognizedDocument = null;
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
      console.log(`ExpenditureService: ${message}`);
  }

}
