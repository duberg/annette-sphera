import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, of } from 'rxjs';
import {environment} from "../../../../../../../environments/environment";
import {HttpUtilsService} from "../../../../components/users/_core/utils/http-utils.service";
import {QueryParamsModel} from "../../../../components/users/_core/models/query-models/query-params.model";
import {QueryResultsModel} from "../../../../components/users/_core/models/query-models/query-results.model";

const API_TENANTS_URL = `${environment.server_addr}/api/tenants`;

@Injectable()
export class TenantsService {
	constructor(private http: HttpClient, private httpUtils: HttpUtilsService) { }

	findTenants(queryParams: QueryParamsModel): Observable<QueryResultsModel> {
		const params = this.httpUtils.getFindHTTPParams(queryParams);
		return this.http.get<QueryResultsModel>(API_TENANTS_URL, { params: params })
	}
}


