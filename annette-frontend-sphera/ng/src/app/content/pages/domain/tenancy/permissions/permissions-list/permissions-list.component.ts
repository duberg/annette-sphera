import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {MatDialog, MatPaginator, MatSnackBar, MatSort} from "@angular/material";
import {SelectionModel} from "@angular/cdk/collections";
import {LayoutUtilsService, MessageType} from "../../../../components/apps/e-commerce/_core/utils/layout-utils.service";
import {TranslateService} from "@ngx-translate/core";
import {fromEvent, merge} from "rxjs";
import {debounceTime, distinctUntilChanged, tap} from "rxjs/operators";
import {QueryParamsModel} from "../../../../components/apps/e-commerce/_core/models/query-models/query-params.model";
import {PermissionsDatasource} from "../../_core/datasources/permissions.datasource";
import {PermissionModel} from "../../_core/permission.model";
import {PermissionsService} from "../../_core/services/permissions.service";

@Component({
  selector: 'm-permissions-list',
  templateUrl: './permissions-list.component.html',
  styleUrls: ['./permissions-list.component.scss']
})
export class PermissionsListComponent implements OnInit {
// Table fields
	dataSource: PermissionsDatasource;
	displayedColumns = ['select', 'id', 'accessPath', 'action'];
	@ViewChild(MatPaginator) paginator: MatPaginator;
	@ViewChild(MatSort) sort: MatSort;
	// Filter fields
	@ViewChild('searchInput') searchInput: ElementRef;
	filterStatus: string = '';
	filterType: string = '';
	// Selection
	selection = new SelectionModel<PermissionModel>(true, []);
	permissionsResult: PermissionModel[] = [];

	constructor(
		private permissionsService: PermissionsService,
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
					this.loadPermissionsList();
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
					this.loadPermissionsList();
				})
			)
			.subscribe();

		// Init DataSource
		const queryParams = new QueryParamsModel(this.filterConfiguration(false));
		this.dataSource = new PermissionsDatasource(this.permissionsService);
		// First load
		this.dataSource.loadPermissions(queryParams);
		this.dataSource.entitySubject.subscribe(res => (this.permissionsResult = res));
	}

	loadPermissionsList() {
		const queryParams = new QueryParamsModel(
			this.filterConfiguration(true),
			this.sort.direction,
			this.sort.active,
			this.paginator.pageIndex,
			this.paginator.pageSize
		);
		this.dataSource.loadPermissions(queryParams);
		this.selection.clear();
	}

	/** FILTRATION */
	filterConfiguration(isGeneralSearch: boolean = true): any {
		const filter: any = {};
		const searchText: string = this.searchInput.nativeElement.value;

		if (this.filterStatus && this.filterStatus.length > 0) {
			filter.status = +this.filterStatus;
		}

		if (this.filterType && this.filterType.length > 0) {
			filter.type = +this.filterType;
		}

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
		const numRows = this.permissionsResult.length;
		return numSelected === numRows;
	}

	masterToggle() {
		if (this.selection.selected.length === this.permissionsResult.length) {
			this.selection.clear();
		} else {
			this.permissionsResult.forEach(row => this.selection.select(row));
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

	addpermission() {

	}

	deletepermissions() {

	}

	fetchpermissions() {

	}

	updateStatusForpermissions() {

	}

	editpermission(permission) {

	}

	deletepermission(permission) {

	}
}
