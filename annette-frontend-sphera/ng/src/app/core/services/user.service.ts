import { Injectable } from '@angular/core';
import {Observable, of} from "rxjs";
import {User} from "../models/model";
import {JsonPipe} from "@angular/common";
import {map} from "rxjs/operators";

@Injectable()
export class UserService {
	USER_KEY = 'user';

	constructor() {}

	public getCurrentUser(): Observable<User> {
		const user = localStorage.getItem(this.USER_KEY);

		//console.log(<User>JSON.parse(user));

		try {
			return of(JSON.parse(user));
		} catch (e) {}

	}

	public getAvatarUrl(): Observable<string> {
		return this.getCurrentUser().pipe(map(x => x.avatarUrl));
	}

	public setCurrentUser(user: User): UserService {
		localStorage.setItem(this.USER_KEY, JSON.stringify(user));
		return this;
	}

	public clear() {
		localStorage.removeItem(this.USER_KEY);
	}

}
