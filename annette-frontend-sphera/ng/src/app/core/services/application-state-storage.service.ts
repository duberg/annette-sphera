import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

@Injectable()
export class ApplicationStateStorage {
	public getAccessToken(): Observable<string> {
		const token: string = <string>localStorage.getItem('accessToken');
		return of(token);
	}

	public setAccessToken(token: string): ApplicationStateStorage {
		localStorage.setItem('accessToken', token);

		return this;
	}

	public clear() {
		localStorage.removeItem('accessToken');
		localStorage.removeItem('refreshToken');
		localStorage.removeItem('userRoles');
	}
}
