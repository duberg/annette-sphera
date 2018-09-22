import { AuthenticationService } from '../../../../../core/auth/authentication.service';
import {
	Component,
	ElementRef,
	HostBinding,
	Input,
	OnInit,
	ViewChild
} from '@angular/core';
import { Router } from '@angular/router';
import { DomSanitizer, SafeStyle } from '@angular/platform-browser';
import {UserService} from "../../../../../core/services/user.service";
import {User} from "../../../../../core/models/model";
import { BehaviorSubject } from 'rxjs';

@Component({
	selector: 'm-user-profile',
	templateUrl: './user-profile.component.html'
	//changeDetection: ChangeDetectionStrategy.Default
})
export class UserProfileComponent implements OnInit {
	@HostBinding('class')
	// tslint:disable-next-line:max-line-length
	classes = 'm-nav__item m-topbar__user-profile m-topbar__user-profile--img m-dropdown m-dropdown--medium m-dropdown--arrow m-dropdown--header-bg-fill m-dropdown--align-right m-dropdown--mobile-full-width m-dropdown--skin-light';

	@HostBinding('attr.m-dropdown-toggle') attrDropdownToggle = 'click';

	@Input() avatar: string = './assets/app/media/img/users/user4.jpg';
	@Input() avatarBg: SafeStyle = '';

	@ViewChild('mProfileDropdown') mProfileDropdown: ElementRef;

	fullname$: BehaviorSubject<string> = new BehaviorSubject('');
	email$: BehaviorSubject<string> = new BehaviorSubject('');

	constructor(
		private router: Router,
		private authService: AuthenticationService,
		private userService: UserService,
		private sanitizer: DomSanitizer
	) {}

	ngOnInit(): void {
		if (!this.avatarBg)
			this.avatarBg = this.sanitizer.bypassSecurityTrustStyle('url(./assets/app/media/img/misc/user_profile_bg.jpg)');

		this.userService.getCurrentUser().subscribe(this.update.bind(this))
	}

	public update(user: User): User {
		this.fullname$.next(`${user.firstname} ${user.lastname}`);
		this.email$.next(user.email);
		return user;
	}

	public logout() {
		this.authService.logout(true);
	}
}
