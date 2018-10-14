import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, of } from 'rxjs';
import { mergeMap, map, flatMap, tap } from 'rxjs/operators';
import {environment} from "../../../../../../../environments/environment";
import {User} from "../user.model";
import {HttpUtilsService} from "../utils/http-utils.service";
import {QueryParamsModel} from "../query-models/query-params.model";
import {QueryResultsModel} from "../query-models/query-results.model";

const API_PERMISSIONS_URL = `${environment.server_addr}/api/permissions`;

@Injectable()
export class PermissionsService {
	constructor(private http: HttpClient, private httpUtils: HttpUtilsService) { }

	listPermissions(queryParams: QueryParamsModel): Observable<QueryResultsModel> {
		const params = this.httpUtils.getFindHTTPParams(queryParams);
		return this.http.get<QueryResultsModel>(API_PERMISSIONS_URL, { params: params })
	}
}


