import {
	Component,
	OnInit,
	Input,
	Output,
	ViewChild,
	ElementRef, ChangeDetectorRef
} from '@angular/core';
import { Subject } from 'rxjs';
import { AuthenticationService } from '../../../../core/auth/authentication.service';
import { NgForm } from '@angular/forms';
import * as objectPath from 'object-path';
import { AuthNoticeService } from '../../../../core/auth/auth-notice.service';
import { SpinnerButtonOptions } from '../../../partials/content/general/spinner-button/button-options.interface';
import { TranslateService } from '@ngx-translate/core';
import {SignUpUser} from "../../../../core/models/user.model";

@Component({
	selector: 'm-register',
	templateUrl: './signup.component.html',
	styleUrls: ['./signup.component.scss']
})
export class SignupComponent implements OnInit {
	public model: SignUpUser = {
		email: '',
		lastName: '',
		firstName: '',
		password: ''
	};

	@Input() action: string;
	@Output() actionChange = new Subject<string>();
	public loading = false;

	@ViewChild('f') f: NgForm;
	errors: any = [];

	spinner: SpinnerButtonOptions = {
		active: false,
		spinnerSize: 18,
		raised: true,
		buttonColor: 'primary',
		spinnerColor: 'accent',
		fullWidth: false
	};

	constructor(
		private authService: AuthenticationService,
		public authNoticeService: AuthNoticeService,
		private translate: TranslateService,
		private cdr: ChangeDetectorRef
	) {}

	ngOnInit() {}

	backToSignInPage(event: Event) {
		event.preventDefault();
		this.action = 'signIn';
		this.actionChange.next(this.action);
	}

	submit() {
		this.spinner.active = true;
		if (this.validate(this.f)) {
			this.authService.signUp(this.model).subscribe(response => {
				if (response !== 'undefined' && !(Number.isInteger(response))) {
					this.action = 'signIn';
					this.actionChange.next(this.action);
					this.authNoticeService.setNotice(this.translate.instant('AUTH.REGISTER.SUCCESS'), 'success');
				}
				else this.authNoticeService.setNotice(this.translate.instant('AUTH.REGISTER.FAILURE'), 'error');

				this.spinner.active = false;
				this.cdr.detectChanges();
			});
		}
	}

	validate(f: NgForm) {
		if (f.form.status === 'VALID') {
			return true;
		}

		this.errors = [];

		if (objectPath.get(f, 'form.controls.firstName.errors.required')) {
			this.errors.push(this.translate.instant('AUTH.VALIDATION.REQUIRED', {name: this.translate.instant('AUTH.INPUT.FIRSTNAME')}));
		}
		if (objectPath.get(f, 'form.controls.lastName.errors.required')) {
			this.errors.push(this.translate.instant('AUTH.VALIDATION.REQUIRED', {name: this.translate.instant('AUTH.INPUT.LASTNAME')}));
		}
		if (objectPath.get(f, 'form.controls.email.errors.email')) {
			this.errors.push(this.translate.instant('AUTH.VALIDATION.INVALID', {name: this.translate.instant('AUTH.INPUT.EMAIL')}));
		}
		if (objectPath.get(f, 'form.controls.email.errors.required')) {
			this.errors.push(this.translate.instant('AUTH.VALIDATION.REQUIRED', {name: this.translate.instant('AUTH.INPUT.EMAIL')}));
		}

		if (objectPath.get(f, 'form.controls.password.errors.required')) {
			this.errors.push(this.translate.instant('AUTH.VALIDATION.REQUIRED', {name: this.translate.instant('AUTH.INPUT.PASSWORD')}));
		}
		if (objectPath.get(f, 'form.controls.password.errors.minlength')) {
			this.errors.push(this.translate.instant('AUTH.VALIDATION.MIN_LENGTH', {name: this.translate.instant('AUTH.INPUT.PASSWORD'), min: 4}));
		}

		if (objectPath.get(f, 'form.controls.rpassword.errors.required')) {
			this.errors.push(this.translate.instant('AUTH.VALIDATION.REQUIRED', {name: this.translate.instant('AUTH.INPUT.CONFIRM_PASSWORD')}));
		}

		if (objectPath.get(f, 'form.controls.agree.errors.required')) {
			this.errors.push(this.translate.instant('AUTH.VALIDATION.AGREEMENT_REQUIRED'));
		}

		if (this.errors.length > 0) {
			this.authNoticeService.setNotice(this.errors.join('<br/>'), 'error');
			this.spinner.active = false;
		}

		return false;
	}
}
