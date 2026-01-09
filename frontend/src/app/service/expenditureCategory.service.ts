
import { Injectable, NgModule } from '@angular/core';
import { HttpClient, HttpClientModule, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class ExpenditureCategoryService {

  private expenditureCategoryUrl = 'http://192.168.178.27:8080/api/expenditureCategory';  // URL to web api

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(private http: HttpClient) { }

  deleteExpenditureCategory(id: number): Observable<string> {
      const url = `${this.expenditureCategoryUrl}/${id}`;
      return this.http.delete(url, { responseType: 'text'}).pipe(
        tap(response => this.log(`Server Response: ${response}`)),
        catchError(this.handleError<string>(`deleteExpenditureCategory id=${id}`))
      );
  }

  getExpenditureSum(id: number): Observable<string> {
    const url = `${this.expenditureCategoryUrl}/${id}/currentMonth`;
    return this.http.get(url, { responseType: 'text'}).pipe(
      tap(response => this.log(`Server Response: ${response}`)),
      catchError(this.handleError<string>(`getExpenditureSum id=${id}`))
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
      console.log(`ExpenditureCategoryService: ${message}`);
  }

}
