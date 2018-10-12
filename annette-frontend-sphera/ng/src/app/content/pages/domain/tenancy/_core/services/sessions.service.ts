import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, of } from 'rxjs';
import {environment} from "../../../../../../../environments/environment";
import {HttpUtilsService} from "../utils/http-utils.service";
import {QueryParamsModel} from "../query-models/query-params.model";
import {QueryResultsModel} from "../query-models/query-results.model";

const API_SESSIONS_URL = `${environment.server_addr}/api/auth/sessions`;

@Injectable()
export class SessionsService {
	constructor(private http: HttpClient, private httpUtils: HttpUtilsService) { }

	listSessions(queryParams: QueryParamsModel): Observable<QueryResultsModel> {
		const params = this.httpUtils.getFindHTTPParams(queryParams);
		return this.http.get<QueryResultsModel>(API_SESSIONS_URL, { params: params })
	}
}


