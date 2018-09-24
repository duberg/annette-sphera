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

import { HttpErrorHandler } from './http-error-handler.service';
import {UNAUTHORIZED} from "http-status-codes";
import {Router} from "@angular/router";
import {AuthenticationService} from "./authentication.service";

@Injectable()
export class RequestInterceptor implements HttpInterceptor {
	constructor(
		private httpErrorHandler : HttpErrorHandler,
		private router: Router,
		private authService: AuthenticationService
	) {}

	intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		const h = request.headers.set('Content-Type', 'application/json; charset=utf-8');
		const r = request.clone({headers: h});
		return next.handle(r).pipe(
			catchError((err: HttpErrorResponse) => {
				if (this.router.url !== '/signin') {
					if (err.status === UNAUTHORIZED) this.authService.logout(true);
					else this.httpErrorHandler.handleError(err);
				}
				return throwError(err);
			})
		);
	}
}
