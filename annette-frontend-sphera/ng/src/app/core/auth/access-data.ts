import {Application, Language, Tenant, TenantData, User} from "../models/model";

export interface AccessData2 {
	accessToken: string;
	refreshToken: string;
	roles: any;
}

export interface AccessData {
	authenticated: boolean,
	language: Language,
	languages: Language[],
	user: User,
	tenant: Tenant,
	application: Application,
	tenantData: TenantData[],
	jwtToken: string,
	accessToken: string;
	refreshToken: string;
	roles: any;
}
