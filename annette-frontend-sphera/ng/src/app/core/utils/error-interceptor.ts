import { Injectable } from '@angular/core';
import {
	HttpRequest,
	HttpHandler,
	HttpEvent,
	HttpInterceptor,
	HttpResponse,
	HttpErrorResponse,
} from '@angular/common/http';

import {Observable, throwError} from "rxjs";
import {tap, catchError} from "rxjs/operators";

import { ErrorHandler } from './error-handler';
import {UNAUTHORIZED} from "http-status-codes";
import {Router} from "@angular/router";
import {AuthenticationService} from "../auth/authentication.service";

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

	constructor(
		private errorHandler : ErrorHandler,
		private router: Router,
		private authService: AuthenticationService
	) {}

	intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		return next.handle(request).pipe(
			catchError((err: HttpErrorResponse) => {
				if (this.router.url !== '/login') {
					if (err.status === 401) this.authService.logout(true);
					else this.errorHandler.handleError(err);
				}
				return throwError(err);
			})
		);
	}
}
