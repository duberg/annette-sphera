import { BehaviorSubject, Observable, Subject, from, throwError } from 'rxjs';
import { map, catchError, tap, switchMap } from 'rxjs/operators';

import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpHeaders} from '@angular/common/http';
import { AuthService } from 'ngx-auth';

import { TokenStorage } from './token-storage.service';
import { UtilsService } from '../services/utils.service';
import {AccessData, AccessData2} from './access-data';
import { Credential } from './credential';
import {Credential2} from "./credential2";
import * as HttpStatus from "http-status-codes";
import { environment } from 'environments/environment';
import {UserService} from "../services/user.service";

@Injectable()
export class AuthenticationService implements AuthService {
	API_URL = `${environment.server_addr}/auth/api`;
	API_ENDPOINT_LOGIN = '/login';
	API_ENDPOINT_REFRESH = '/refresh';
	API_ENDPOINT_REGISTER = '/register';

	public onCredentialUpdated$: Subject<AccessData>;

	constructor(
		private http: HttpClient,
		private tokenStorage: TokenStorage,
		private userService: UserService,
		private util: UtilsService
	) {
		this.onCredentialUpdated$ = new Subject();
	}

	/**
	 * Check, if user already authorized.
	 * @description Should return Observable with true or false values
	 * @returns {Observable<boolean>}
	 * @memberOf AuthService
	 */
	public isAuthorized(): Observable<boolean> {
		return this.tokenStorage.getAccessToken().pipe(map(token => !!token));
	}

	/**
	 * Get access token
	 * @description Should return access token in Observable from e.g. localStorage
	 * @returns {Observable<string>}
	 */
	public getAccessToken(): Observable<string> {
		return this.tokenStorage.getAccessToken();
	}

	/**
	 * Get user roles
	 * @returns {Observable<any>}
	 */
	public getUserRoles(): Observable<any> {
		return this.tokenStorage.getUserRoles();
	}

	/**
	 * Function, that should perform refresh token verifyTokenRequest
	 * @description Should be successfully completed so interceptor
	 * can execute pending requests or retry original one
	 * @returns {Observable<any>}
	 */
	public refreshToken(): Observable<AccessData> {
		return this.tokenStorage.getRefreshToken().pipe(
			switchMap((refreshToken: string) => {
				return this.http.get<AccessData>(this.API_URL + this.API_ENDPOINT_REFRESH + '?' + this.util.urlParam(refreshToken));
			}),
			tap(this.saveAccessData.bind(this)),
			catchError(err => {
				this.logout();
				return throwError(err);
			})
		);
	}

	/**
	 * Function, checks response of failed request to determine,
	 * whether token be refreshed or not.
	 * @description Essentialy checks status
	 * @param {Response} response
	 * @returns {boolean}
	 */
	public refreshShouldHappen(response: HttpErrorResponse): boolean {
		return response.status === 401;
	}

	/**
	 * Verify that outgoing request is refresh-token,
	 * so interceptor won't intercept this request
	 * @param {string} url
	 * @returns {boolean}
	 */
	public verifyTokenRequest(url: string): boolean {
		return url.endsWith(this.API_ENDPOINT_REFRESH);
	}

	/**
	 * Submit login request
	 * @param {Credential} credential
	 * @returns {Observable<any>}
	 */
	public login(credential: Credential): Observable<any> {
		return this.http.get<AccessData>(this.API_URL + this.API_ENDPOINT_LOGIN + '?' + this.util.urlParam(credential)).pipe(
			map((result: any) => {
				if (result instanceof Array) {
					return result.pop();
				}
				return result;
			}),
			tap(this.saveAccessData.bind(this)),
			catchError(this.handleError('login', []))
		);
	}

	public login2(credential: Credential2): Observable<any> {
		console.info("Try to login...");
		console.info(credential);

		const headers = new HttpHeaders({'Content-Type':'application/json; charset=utf-8'});
		const b = JSON.stringify(credential);
		const h = { headers: headers };
		console.info(this.API_URL + this.API_ENDPOINT_LOGIN);
		return this.http.post<AccessData>(this.API_URL + this.API_ENDPOINT_LOGIN, b, h).pipe(
			map((result: any) => {
				console.log("result: " + result);
				if (result instanceof Array) {
					return result.pop();
				}
				return result;
			}),
			tap(this.saveAccessData.bind(this)),
			catchError(this.handleError('login', []))
		);


		// get<AccessData>(this.API_URL + this.API_ENDPOINT_LOGIN + '?' + this.util.urlParam(credential)).pipe(
		// 	map((result: any) => {
		// 		if (result instanceof Array) {
		// 			return result.pop();
		// 		}
		// 		return result;
		// 	}),
		// 	tap(this.saveAccessData.bind(this)),
		// 	catchError(this.handleError('login', []))
		// );
	}

	/**
	 * Handle Http operation that failed.
	 * Let the app continue.
	 * @param operation - name of the operation that failed
	 * @param result - optional value to return as the observable result
	 */
	private handleError<T>(operation = 'operation', result?: any) {
		return (error: HttpErrorResponse): Observable<any> => {

			return from([error.status]);

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

	/**
	 * Logout
	 */
	public logout(refresh?: boolean): void {
		this.tokenStorage.clear();
		if (refresh) {
			location.reload(true);
		}
	}

	/**
	 * Save access data in the storage
	 * @private
	 * @param {AccessData} accessData
	 */
	private saveAccessData(accessData: AccessData) {
		if (typeof accessData !== 'undefined') {
			this.tokenStorage
				.setAccessToken(accessData.jwtToken)
				.setRefreshToken(accessData.jwtToken)
				.setUserRoles(['ADMIN']);

			this.userService.setCurrentUser(accessData.user);

			this.onCredentialUpdated$.next(accessData);
		}
	}

	/**
	 * Submit registration request
	 * @param {Credential} credential
	 * @returns {Observable<any>}
	 */
	public register(credential: Credential): Observable<any> {
		// dummy token creation
		credential = Object.assign({}, credential, {
			accessToken: 'access-token-' + Math.random(),
			refreshToken: 'access-token-' + Math.random(),
			roles: ['USER'],
		});
		return this.http.post(this.API_URL + this.API_ENDPOINT_REGISTER, credential)
			.pipe(catchError(this.handleError('register', []))
		);
	}

	/**
	 * Submit forgot password request
	 * @param {Credential} credential
	 * @returns {Observable<any>}
	 */
	public requestPassword(credential: Credential): Observable<any> {
		return this.http.get(this.API_URL + this.API_ENDPOINT_LOGIN + '?' + this.util.urlParam(credential))
			.pipe(catchError(this.handleError('forgot-password', []))
		);
	}

}