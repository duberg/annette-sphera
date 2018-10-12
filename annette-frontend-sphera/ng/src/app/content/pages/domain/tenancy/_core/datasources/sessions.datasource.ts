import { Observable, of } from 'rxjs';
import {catchError, finalize, map, tap} from 'rxjs/operators';
import {BaseDataSource} from "../../../../../../core/datasources/base.datasource";
import {QueryParamsModel} from "../../../../components/users/_core/models/query-models/query-params.model";
import {QueryResultsModel} from "../../../../components/apps/e-commerce/_core/models/query-models/query-results.model";
import {SessionsService} from "../services/sessions.service";

export class SessionsDatasource extends BaseDataSource {
	constructor(private sessionsService: SessionsService) {
		super();
	}

	loadSessions(queryParams: QueryParamsModel): void {
		this.loadingSubject.next(true);
		this.sessionsService.listSessions(queryParams).pipe(
			tap(res => {
				this.entitySubject.next(res.items);
				this.paginatorTotalSubject.next(res.totalCount);
			}),
			catchError(err => of(new QueryResultsModel([], err))),
			finalize(() => this.loadingSubject.next(false))
		).subscribe();
	}
}
