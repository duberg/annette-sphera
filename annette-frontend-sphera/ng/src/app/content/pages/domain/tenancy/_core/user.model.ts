import { BaseModel } from './_base.model';

export class User extends BaseModel {
	id?: string;
	username?: string;
	displayName?: string;
	firstName?: string;
	lastName?: string;
	middleName?: string;
	gender?: string;
	email?: string;
	url?: string;
	description?: string;
	phone?: string;
	language?: string;
	registeredDate?: string;
	roles?: {};
	password?: string;
	avatarUrl?: string;
	sphere?: string;
	company?: string;
	position?: string;
	rank?: string;
	additionalTel?: string;
	additionalMail?: string;
	meta?: {};
	status?: number; // 0 = Active | 1 = Suspended | Pending = 2

	//: string;
	dob?: Date;
	type?: number; // 0 = Business | 1 = Individual
	dateOfBbirth?: string;

	clear() {
		//this.dob = new Date();
		this.username = '';
		this.displayName = '';
		this.firstName = '';
		this.lastName = '';
		this.middleName = '';
		this.gender = '';
		this.email = '';
		this.url = '';
		this.description = '';
		this.phone = '';
		this.language = '';
		this.registeredDate = '';
		this.roles = {};
		this.password = '';
		this.avatarUrl = '';
		this.sphere = '';
		this.company = '';
		this.position = '';
		this.rank = '';
		this.additionalTel = '';
		this.additionalMail = '';
		this.meta = {};
		this.status = 0;

		return this;
	}
}

export interface CreateUser extends BaseModel {
	username?: string;
	displayName?: string;
	firstName: string;
	lastName: string;
	middleName?: string;
	gender?: string;
	email: string;
	url?: string;
	description?: string;
	phone?: string;
	language?: string;
	roles: {};
	password: string;
	avatarUrl?: string;
	sphere?: string;
	company?: string;
	position?: string;
	rank?: string;
	additionalTel?: string;
	additionalMail?: string;
	meta: {};
	status: number;
}

export interface UpdateUser extends BaseModel {
	id: string;
	username?: string;
	displayName?: string;
	firstName?: string;
	lastName?: string;
	middleName?: string;
	gender?: string;
	email?: string;
	url?: string;
	description?: string;
	phone?: string;
	language?: string;
	registeredDate?: string;
	roles?: {};
	password?: string;
	avatarUrl?: string;
	sphere?: string;
	company?: string;
	position?: string;
	rank?: string;
	additionalTel?: string;
	additionalMail?: string;
	meta?: {};
	status?: number;
}
