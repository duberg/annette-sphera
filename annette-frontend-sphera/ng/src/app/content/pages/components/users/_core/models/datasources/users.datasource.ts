import { Observable, of } from 'rxjs';
import {catchError, finalize, map, tap} from 'rxjs/operators';
import { UsersService } from '../../../../../../../core/services/users.service';
import { QueryParamsModel } from '../query-models/query-params.model';
import { QueryResultsModel } from '../query-models/query-results.model';
import {BaseDataSource} from "../../../../../../../core/datasources/base.datasource";

export class UsersDatasource extends BaseDataSource {
	constructor(private customersService: UsersService) {
		super();
	}

	loadCustomers(queryParams: QueryParamsModel): void {
		this.loadingSubject.next(true);
		this.customersService.findCustomers(queryParams).pipe(
			tap(res => {
				// Comment this when you start work with real server
				// This code imitates server calls
				// START
				//const result = this.baseFilter(res.items, queryParams, ['status', 'type']);
				//this.entitySubject.next(result.items);
				//this.paginatorTotalSubject.next(result.totalCount);
				// END

				// Uncomment this when you start work with real server
				// START
				this.entitySubject.next(res.items);
				this.paginatorTotalSubject.next(res.totalCount);
				// END
			}),
			catchError(err => of(new QueryResultsModel([], err))),
			finalize(() => this.loadingSubject.next(false))
		).subscribe();
	}
}
