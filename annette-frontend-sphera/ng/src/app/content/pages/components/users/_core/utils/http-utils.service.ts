import { Injectable } from '@angular/core';
import { HttpParams, HttpHeaders } from '@angular/common/http';
import {AuthenticationService} from "../../../../../../core/auth/authentication.service";
import {map} from "rxjs/operators";
import {Observable} from "rxjs";


@Injectable()
export class HttpUtilsService {
	constructor(private authService: AuthenticationService) {

	}

	getFindHTTPParams(queryParams): HttpParams {
		const params = new HttpParams()
			.set('filter', queryParams.filter)
			.set('sortOrder', queryParams.sortOrder)
			.set('sortField', queryParams.sortField)
			.set('pageNumber', queryParams.pageNumber.toString())
			.set('pageSize', queryParams.pageSize.toString());

		return params;
	}

	toHttpHeaders(accessToken: string): { headers: HttpHeaders } {
		const headers: HttpHeaders =  new HttpHeaders({
			'Content-Type': 'application/json',
			'X-Token': accessToken
		});
		return {headers: headers};
	}

	getHTTPHeader() {
		return this.authService.getAccessToken().pipe(map(this.toHttpHeaders));
	}
}
