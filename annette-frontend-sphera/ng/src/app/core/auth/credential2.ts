export interface Credential2 {
	login: string,
	password: string,
	rememberMe: boolean,
	language: string,
	selectTenant: boolean,
	tenant?: string,
	application?: string
}
