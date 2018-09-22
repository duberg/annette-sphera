import { Injectable } from '@angular/core';
import {Observable, of} from "rxjs";
import {User} from "../models/model";
import {JsonPipe} from "@angular/common";

@Injectable()
export class UserService {
	USER_KEY = 'user';

	constructor() {}

	public getCurrentUser(): Observable<User> {
		const user = localStorage.getItem(this.USER_KEY);

		console.log("dddddd");
		console.log(<User>JSON.parse(user));

		try {
			return of(JSON.parse(user));
		} catch (e) {}

	}

	public setCurrentUser(user: User): UserService {
		console.log("1dddddd1");
		localStorage.setItem(this.USER_KEY, JSON.stringify(user));
		return this;
	}

	public clear() {
		localStorage.removeItem(this.USER_KEY);
	}

}
