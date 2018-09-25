import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material';
import {HttpErrorResponse} from "@angular/common/http";
import {from, Observable, of} from "rxjs";

@Injectable()
export class HttpErrorHandler {
	constructor(
		public snackbar: MatSnackBar,
	) {}

	/**
	 * Handle Http operation that failed.
	 * Let the app continue.
	 * @param operation - name of the operation that failed
	 * @param result - optional value to return as the observable result
	 */
	private handleError1<T>(operation = 'operation', result?: any): (error: HttpErrorResponse) => Observable<number> {
		return (error: HttpErrorResponse): Observable<number> => {
			return of(1);

			// if (error.status == HttpStatus.UNAUTHORIZED) {
			// 	return from([error.status]);
			// } else {
			// 	// TODO: send the error to remote logging infrastructure
			// 	console.error(error); // log to console instead
			// }
			//
			// // Let the app keep running by returning an empty result.
			// return from(result);
		};
	}

	public handleError(err: any) {
		console.log(err);
		this.snackbar.open(err.message, 'close');
	}
}

