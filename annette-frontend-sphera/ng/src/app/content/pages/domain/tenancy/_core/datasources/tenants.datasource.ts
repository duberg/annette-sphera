import { Observable, of } from 'rxjs';
import {catchError, finalize, map, tap} from 'rxjs/operators';
import {TenantsService} from "../services/tenants.service";
import {BaseDataSource} from "../../../../../../core/datasources/base.datasource";
import {QueryParamsModel} from "../query-models/query-params.model";
import {QueryResultsModel} from "../../../../components/apps/e-commerce/_core/models/query-models/query-results.model";

export class TenantsDatasource extends BaseDataSource {
	constructor(private tenantsService: TenantsService) {
		super();
	}

	loadTenants(queryParams: QueryParamsModel): void {
		this.loadingSubject.next(true);
		this.tenantsService.listTenants(queryParams).pipe(
			tap(res => {
				this.entitySubject.next(res.items);
				this.paginatorTotalSubject.next(res.totalCount);
			}),
			catchError(err => of(new QueryResultsModel([], err))),
			finalize(() => this.loadingSubject.next(false))
		).subscribe();
	}
}
