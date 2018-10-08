import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, of } from 'rxjs';
import { mergeMap, map, flatMap, tap } from 'rxjs/operators';
import {environment} from "../../../../../../../environments/environment";
import {UserModel} from "../../../../components/users/_core/models/user.model";
import {HttpUtilsService} from "../../../../components/users/_core/utils/http-utils.service";
import {QueryParamsModel} from "../../../../components/users/_core/models/query-models/query-params.model";
import {QueryResultsModel} from "../../../../components/users/_core/models/query-models/query-results.model";

const API_PERMISSIONS_URL = `${environment.server_addr}/api/permissions`;

@Injectable()
export class PermissionsService {
	constructor(private http: HttpClient, private httpUtils: HttpUtilsService) { }

	findPermissions(queryParams: QueryParamsModel): Observable<QueryResultsModel> {
		const params = this.httpUtils.getFindHTTPParams(queryParams);
		return this.http.get<QueryResultsModel>(API_PERMISSIONS_URL, { params: params })
	}
}


