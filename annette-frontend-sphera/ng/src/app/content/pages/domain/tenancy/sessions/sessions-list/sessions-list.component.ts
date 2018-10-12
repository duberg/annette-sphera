import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {MatDialog, MatPaginator, MatSnackBar, MatSort} from "@angular/material";
import {SelectionModel} from "@angular/cdk/collections";
import {LayoutUtilsService} from "../../_core/utils/layout-utils.service";
import {TranslateService} from "@ngx-translate/core";
import {fromEvent, merge} from "rxjs";
import {debounceTime, distinctUntilChanged, tap} from "rxjs/operators";
import {QueryParamsModel} from "../../_core/query-models/query-params.model";
import {SessionModel} from "../../_core/session.model";
import {SessionsDatasource} from "../../_core/datasources/sessions.datasource";
import {SessionsService} from "../../_core/services/sessions.service";

@Component({
	selector: 'm-sessions-list',
	templateUrl: './sessions-list.component.html',
	styleUrls: ['./sessions-list.component.scss']
})
export class SessionsListComponent implements OnInit {
	dataSource: SessionsDatasource;

	displayedColumns = [
		'select',
		'id',
		'userId',
		'tenantId',
		'applicationId',
		'languageId',
		'startTimestamp',
		'lastOpTimestamp',
		'rememberMe',
		'ip',
		'timestamp'
	];

	@ViewChild(MatPaginator) paginator: MatPaginator;
	@ViewChild(MatSort) sort: MatSort;
	// Filter fields
	@ViewChild('searchInput') searchInput: ElementRef;
	filterStatus: string = '';
	filterType: string = '';
	// Selection
	selection = new SelectionModel<SessionModel>(true, []);
	sessionsResult: SessionModel[] = [];

	constructor(
		private sessionsService: SessionsService,
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
					this.loadSessionsList();
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
					this.loadSessionsList();
				})
			)
			.subscribe();

		// Init DataSource
		const queryParams = new QueryParamsModel(this.filterConfiguration(false));
		this.dataSource = new SessionsDatasource(this.sessionsService);
		// First load
		this.dataSource.loadSessions(queryParams);
		this.dataSource.entitySubject.subscribe(res => (this.sessionsResult = res));
	}

	loadSessionsList() {
		const queryParams = new QueryParamsModel(
			this.filterConfiguration(true),
			this.sort.direction,
			this.sort.active,
			this.paginator.pageIndex,
			this.paginator.pageSize
		);
		this.dataSource.loadSessions(queryParams);
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
		const numRows = this.sessionsResult.length;
		return numSelected === numRows;
	}

	masterToggle() {
		if (this.selection.selected.length === this.sessionsResult.length) {
			this.selection.clear();
		} else {
			this.sessionsResult.forEach(row => this.selection.select(row));
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

	addsession() {

	}

	deletesessions() {

	}

	fetchsessions() {

	}

	updateStatusForsessions() {

	}

	editsession(session) {

	}

	deletesession(session) {

	}
}
