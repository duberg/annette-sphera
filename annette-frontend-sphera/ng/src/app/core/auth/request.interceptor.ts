import { Injectable } from '@angular/core';
import {
	HttpRequest,
	HttpHandler,
	HttpEvent,
	HttpInterceptor,
	HttpResponse,
	HttpErrorResponse,
} from '@angular/common/http';

import {from, Observable, throwError} from "rxjs";
import {tap, catchError} from "rxjs/operators";

import { HttpErrorHandler } from './http-error-handler.service';
import {UNAUTHORIZED} from "http-status-codes";
import {Router} from "@angular/router";
import {AuthenticationService} from "./authentication.service";
import {UtilsService} from "../services/utils.service";
import {TranslateService} from "@ngx-translate/core";
import * as _ from "lodash";

@Injectable()
export class RequestInterceptor implements HttpInterceptor {
	constructor(
		private httpErrorHandler : HttpErrorHandler,
		private router: Router,
		private authService: AuthenticationService,
		private utils: UtilsService,
		private translate: TranslateService
	) {}

	intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		const body = JSON.parse(request.body.toString());

		// Filter unwanted object fields
		const b = _(body)
			.pickBy((value, key) => !_.startsWith(key, '_'))
			.omitBy(_.isNil)
			.value();

		// Add request headers
		const h = request.headers.set('Content-Type', 'application/json; charset=utf-8');

		// Update request
		const r = request.clone({body: b, headers: h});

		return next.handle(r).pipe(
			catchError((err: HttpErrorResponse) => {
				if (this.router.url !== '/signin') {
					if (err.status === UNAUTHORIZED) this.authService.logout(true);
					else this.httpErrorHandler.handleError(err);
				}
				// map json to app error
				const code = err.error.code ? err.error.code : 'core.exceptions.UnknownException';
				const parameters = err.error.parameters ? err.error.parameters : {};
				const message = this.translate.instant(code, parameters);
				const error = new Error(message);
				//console.log(error);
				return throwError(error);
			})
		);
	}
}
