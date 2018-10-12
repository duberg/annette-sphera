import { Injectable } from '@angular/core';
import { HttpParams, HttpHeaders } from '@angular/common/http';
import {AuthenticationService} from "../../../../../../core/auth/authentication.service";
import {map} from "rxjs/operators";
import {Observable} from "rxjs";
import {QueryParamsModel} from "../query-models/query-params.model";


@Injectable()
export class HttpUtilsService {
	constructor(private authService: AuthenticationService) {

	}

	getFindHTTPParams(queryParams: QueryParamsModel): HttpParams {
		let filter = "";
		const offset = queryParams.pageNumber * queryParams.pageSize;

		let params = new HttpParams()
			.set('offset', offset.toString())
			.set('limit', queryParams.pageSize.toString())
			.set('sort', `${queryParams.sortField},${queryParams.sortOrder}`);

		Object.keys(queryParams.filter).forEach(key =>{
			const value = queryParams.filter[key];
			if (value != "") {
				filter += filter != "" ? ";" : "";
				filter += `${key},${value}`
			}
		});

		params = filter == "" ? params : params.set('filter', filter);

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
