export interface User {
	id: string;
  	lastName: string;
  	firstName: string;
  	middleName?: string;
  	avatarUrl?: string;
  	email?: string;
 	phone?: string;

}

export interface SignUpUser {
	email: string;
	lastName: string;
	firstName: string;
	password: string;
	tenants: string[];
	rpassword: '',
	agree: boolean
}



