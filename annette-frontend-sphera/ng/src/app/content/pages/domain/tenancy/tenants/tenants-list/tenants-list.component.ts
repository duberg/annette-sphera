import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {MatDialog, MatPaginator, MatSnackBar, MatSort} from "@angular/material";
import {SelectionModel} from "@angular/cdk/collections";
import {LayoutUtilsService} from "../../_core/utils/layout-utils.service";
import {TranslateService} from "@ngx-translate/core";
import {fromEvent, merge} from "rxjs";
import {debounceTime, distinctUntilChanged, tap} from "rxjs/operators";
import {TenantModel} from "../../_core/tenant.model";
import {TenantsDatasource} from "../../_core/datasources/tenants.datasource";
import {TenantsService} from "../../_core/services/tenants.service";
import {QueryParamsModel} from "../../_core/query-models/query-params.model";

@Component({
  selector: 'm-tenants-list',
  templateUrl: './tenants-list.component.html',
  styleUrls: ['./tenants-list.component.scss']
})
export class TenantsListComponent implements OnInit {
	dataSource: TenantsDatasource;

	displayedColumns = [
		'select',
		'id',
		'name',
		'defaultApplicationId',
		'applications',
		'defaultLanguageId',
		'languages'
	];

	@ViewChild(MatPaginator) paginator: MatPaginator;
	@ViewChild(MatSort) sort: MatSort;
	// Filter fields
	@ViewChild('searchInput') searchInput: ElementRef;
	filterStatus: string = '';
	filterType: string = '';
	// Selection
	selection = new SelectionModel<TenantModel>(true, []);
	tenantsResult: TenantModel[] = [];

	constructor(
		private tenantsService: TenantsService,
		public dialog: MatDialog,
		public snackBar: MatSnackBar,
		private layoutUtilsService: LayoutUtilsService,
		private translate: TranslateService
	) {}

	/** LOAD DATA */
	ngOnInit() {
		// If the user changes the sort order, reset back to the first page.
		this.sort.sortChange.subscribe(() => (this.paginator.pageIndex = 0));

		/* Data load will be triggered in two cases:
		- when a pagination event occurs => this.paginator.page
		- when a sort event occurs => this.sort.sortChange
		**/
		merge(this.sort.sortChange, this.paginator.page)
			.pipe(
				tap(() => {
					this.loadTenantsList();
				})
			)
			.subscribe();

		// Filtration, bind to searchInput
		fromEvent(this.searchInput.nativeElement, 'keyup')
			.pipe(
				// tslint:disable-next-line:max-line-length
				debounceTime(150), // The user can type quite quickly in the input box, and that could trigger a lot of server requests. With this operator, we are limiting the amount of server requests emitted to a maximum of one every 150ms
				distinctUntilChanged(), // This operator will eliminate duplicate values
				tap(() => {
					this.paginator.pageIndex = 0;
					this.loadTenantsList();
				})
			)
			.subscribe();

		// Init DataSource
		const queryParams = new QueryParamsModel(this.filterConfiguration(false));
		this.dataSource = new TenantsDatasource(this.tenantsService);
		// First load
		this.dataSource.loadTenants(queryParams);
		this.dataSource.entitySubject.subscribe(res => (this.tenantsResult = res));
	}

	loadTenantsList() {
		const queryParams = new QueryParamsModel(
			this.filterConfiguration(true),
			this.sort.direction,
			this.sort.active,
			this.paginator.pageIndex,
			this.paginator.pageSize
		);
		this.dataSource.loadTenants(queryParams);
		this.selection.clear();
	}

	/** FILTRATION */
	filterConfiguration(isGeneralSearch: boolean = true): any {
		const filter: any = {};
		const searchText: string = this.searchInput.nativeElement.value;

		if (this.filterStatus) filter.status = this.filterStatus;
		if (this.filterType) filter.type = this.filterType;


		filter.lastName = searchText;
		if (!isGeneralSearch) {
			return filter;
		}

		filter.firstName = searchText;
		filter.email = searchText;
		filter.ipAddress = searchText;
		return filter;
	}


	/** SELECTION */
	isAllSelected(): boolean {
		const numSelected = this.selection.selected.length;
		const numRows = this.tenantsResult.length;
		return numSelected === numRows;
	}

	masterToggle() {
		if (this.selection.selected.length === this.tenantsResult.length) {
			this.selection.clear();
		} else {
			this.tenantsResult.forEach(row => this.selection.select(row));
		}
	}

	/** UI */
	getItemCssClassByStatus(status: number = 0): string {
		switch (status) {
			case 0:
				return 'metal';
			case 1:
				return 'success';
			case 2:
				return 'danger';
		}
		return '';
	}

	getItemStatusString(status: number = 0): string {
		switch (status) {
			case 0:
				return 'Suspended';
			case 1:
				return 'Active';
			case 2:
				return 'Pending';
		}
		return '';
	}

	getItemCssClassByType(status: number = 0): string {
		switch (status) {
			case 0:
				return 'accent';
			case 1:
				return 'primary';
			case 2:
				return '';
		}
		return '';
	}

	getItemTypeString(status: number = 0): string {
		switch (status) {
			case 0:
				return 'Business';
			case 1:
				return 'Individual';
		}
		return '';
	}

	addtenant() {

	}

	deletetenants() {

	}

	fetchtenants() {

	}

	updateStatusFortenants() {

	}

	edittenant(tenant) {

	}

	deletetenant(tenant) {

	}
}
