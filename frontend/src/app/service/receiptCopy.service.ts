
import { Injectable, NgModule } from '@angular/core';
import { HttpClient, HttpClientModule, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { ExpenditureCategory } from '../model/expenditureCategory';
import { ReceiptCopy } from '../model/receiptCopy';

// @NgModule({
//     imports: [
//     BrowserModule,
//     HttpClientModule,
//     ],
// })
@Injectable({ providedIn: 'root' })
export class ReceiptCopyService {

  private receiptCopyUrl = 'http://localhost:8080/api/receiptCopy';  // URL to web api

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(private http: HttpClient) { }

    getReceiptCopyImage(id: number): Observable<Blob> {
        const url = `${this.receiptCopyUrl}/${id}/image`;
        return this.http.get(url, { responseType: 'blob' }).pipe(
          tap(() => this.log(`Fetched image for receiptCopy id=${id}`)),
          catchError(this.handleError<Blob>(`getReceiptCopyImage id=${id}`))
        );
    }

    deleteReceiptCopy(id: number): Observable<string> {
        const url = `${this.receiptCopyUrl}/${id}`;
        return this.http.delete(url, { responseType: 'text'}).pipe(
          tap(response => this.log(`Server Response: ${response}`)),
          catchError(this.handleError<string>(`deleteReceiptCopy id=${id}`))
        );
    }

    translateReceiptCopy(id: number): Observable<string> {
      const url = `${this.receiptCopyUrl}/${id}/translate`;
      return this.http.get(url, { responseType: 'text'}).pipe(
        tap(response => this.log(`Server Response: ${response}`)),
        catchError(this.handleError<string>(`translateReceiptCopy id=${id}`))
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
      console.log(`ReceiptCopyService: ${message}`);
  }

}