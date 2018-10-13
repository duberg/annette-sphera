import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, of } from 'rxjs';
import { mergeMap, map, flatMap, tap } from 'rxjs/operators';
import {environment} from "../../../environments/environment";
import {UpdateUser, UserModel} from "../../content/pages/domain/tenancy/_core/user.model";
import {HttpUtilsService} from "../../content/pages/domain/tenancy/_core/utils/http-utils.service";
import {QueryParamsModel} from "../../content/pages/domain/tenancy/_core/query-models/query-params.model";
import {QueryResultsModel} from "../../content/pages/domain/tenancy/_core/query-models/query-results.model";

const API_USERS_URL = `${environment.server_addr}/api/users`;

@Injectable()
export class UsersService {
	constructor(private http: HttpClient, private httpUtils: HttpUtilsService) { }

	// CREATE =>  POST: add a new customer to the server
	createCustomer(customer: UserModel): Observable<UserModel> {
		return this.httpUtils.getHTTPHeader().pipe(flatMap(headers => {
			return this.http.post<UserModel>(API_USERS_URL, customer, headers);
		}));
	}

	// READ
	getAllCustomers(): Observable<QueryResultsModel> {
		return this.http.get<QueryResultsModel>(API_USERS_URL);
	}

	getCustomerById(customerId: number): Observable<UserModel> {
		return this.http.get<UserModel>(API_USERS_URL + `/${customerId}`);
	}

	// Method from server should return QueryResultsModel(any[], totalsCount: number)
	listUsers(queryParams: QueryParamsModel): Observable<QueryResultsModel> {
		const params = this.httpUtils.getFindHTTPParams(queryParams);
		return this.http.get<QueryResultsModel>(API_USERS_URL, { params: params })
		// 	.pipe(
		// 	map(res => new QueryResultsModel(res.items, res.totalCount))
		// );
	}

	// UPDATE => PUT: update the customer on the server
	updateUser(user: UpdateUser): Observable<any> {
		const url = `${API_USERS_URL}/${user.id}`;
		return this.http.post(url, user);
	}

	// UPDATE Status
	// Comment this when you start work with real server
	// This code imitates server calls
	updateStatusForCustomer(customers: UserModel[], status: number): Observable<any> {
		// const tasks$ = [];
		// for (let i = 0; i < customers.length; i++) {
		// 	const _user = customers[i];
		// 	_user.status = status;
		// 	tasks$.push(this.updateUser(_user));
		// }
		// return forkJoin(tasks$);
		return of('');
	}

	// DELETE => delete the customer from the server
	deleteCustomer(customerId: string): Observable<any> {
		const url = `${API_USERS_URL}/${customerId}`;
		return this.http.delete(url);
	}

	// Method imitates server calls which deletes items from DB (should rewrite this to real server call)
	// START
	deleteCustomers(ids: string[] = []) {
		// Comment this when you start work with real server
		// This code imitates server calls
		// START
		const tasks$ = [];
		const length = ids.length;
		for (let i = 0; i < length; i++) {
			tasks$.push(this.deleteCustomer(ids[i]));
		}
		return forkJoin(tasks$);
		// END

		// Uncomment this when you start work with real server
		// Note: Add headers if needed
		// START
		// const url = this.API_CUSTOMERS_URL + '/delete';
		// return this.http.get<QueryResultsModel>(url, { params: ids });
		// END
	}
}


