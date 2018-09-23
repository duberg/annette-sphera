
export interface ApplicationState {
  authenticated: boolean,
  language: Language,
  languages: Language[],
  user?: User,
  tenant?: Tenant
  application?: Application,
  tenantData: TenantData[],
  jwtToken?: string
}

export interface TenantData {
  name: string;
  apps: Application[];
  application: Application,
  lang: Language,
  langs: Language[],
  id: string;
}

export interface Tenant {
  name: string,
  applicationId?: string,
  applications?: string[],
  languageId?: string,
  languages?: string[],
  id: string
}

export interface Application {
  name: string;
  code: string;
  id: string;
}

export interface Language {
  name: string;
  id: string;
}

export interface User {
  lastName: string;
  firstName: string;
  middleName?: string;
  email?: string;
  phone?: string;
  id?: string;
}

export interface SetApplicationState {
  tenantId: string,
  applicationId: string,
  languageId: string
}

export interface LoginData {
  login: string,
  password: string,
  rememberMe: boolean,
  language: string,
  selectTenant: boolean,
  tenant?: string,
  application?: string

}

export interface LoginStatus {
  authenticated: boolean,
  errorMessage?: any
  tenantData?: TenantData[]
}



