interface Filter {
	status: number;
	type: number;
	firstName: String,
	email: String
}

export class QueryParamsModel {
	// fields
	filter: Filter;
	sortOrder: string; // asc || desc
	sortField: string;
	pageNumber: number;
	pageSize: number;
	search?: string;

	// constructor overrides
	constructor(_filter: Filter,
		_sortOrder: string = 'asc',
		_sortField: string = '',
		_pageNumber: number = 0,
		_pageSize: number = 10,
		_search: string = '') {
		this.filter = _filter;
		this.sortOrder = _sortOrder;
		this.sortField = _sortField;
		this.pageNumber = _pageNumber;
		this.pageSize = _pageSize;
		this.search = _search;
	}
}
