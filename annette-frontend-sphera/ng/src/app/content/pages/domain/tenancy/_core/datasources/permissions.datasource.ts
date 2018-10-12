import { Observable, of } from 'rxjs';
import {catchError, finalize, map, tap} from 'rxjs/operators';
import {PermissionsService} from "../services/permissions.service";
import {BaseDataSource} from "../../../../../../core/datasources/base.datasource";
import {QueryParamsModel} from "../../../../components/users/_core/models/query-models/query-params.model";
import {QueryResultsModel} from "../../../../components/apps/e-commerce/_core/models/query-models/query-results.model";

export class PermissionsDatasource extends BaseDataSource {
	constructor(private permissionsService: PermissionsService) {
		super();
	}

	loadPermissions(queryParams: QueryParamsModel): void {
		this.loadingSubject.next(true);
		this.permissionsService.listPermissions(queryParams).pipe(
			tap(res => {
				this.entitySubject.next(res.items);
				this.paginatorTotalSubject.next(res.totalCount);
			}),
			catchError(err => of(new QueryResultsModel([], err))),
			finalize(() => this.loadingSubject.next(false))
		).subscribe();
	}
}
