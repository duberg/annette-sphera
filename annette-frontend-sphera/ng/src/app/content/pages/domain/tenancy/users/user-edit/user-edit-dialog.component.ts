import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {TypesUtilsService} from '../../_core/utils/types-utils.service';
import {CreateUser, UpdateUser, User} from '../../_core/user.model';
import {UsersService} from "../../../../../../core/services/users.service";
import {addedDiff, updatedDiff, diff} from 'deep-object-diff';
import * as _ from 'lodash';

@Component({
	selector: 'm-customers-edit-dialog',
	templateUrl: './user-edit-dialog.component.html',
	// changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserEditDialogComponent implements OnInit {
	user: User;
	customerForm: FormGroup;
	hasFormErrors: boolean = false;
	viewLoading: boolean = false;
	loadingAfterSubmit: boolean = false;

	constructor(public dialogRef: MatDialogRef<UserEditDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any,
		private fb: FormBuilder,
		private customerService: UsersService,
		private typesUtilsService: TypesUtilsService) { }

	/** LOAD DATA */
	ngOnInit() {
		this.user = this.data.user;
		this.createForm();

		/* Server loading imitation. Remove this on real code */
		this.viewLoading = true;
		setTimeout(() => {
			this.viewLoading = false;
		}, 1000);
	}

	createForm() {
		this.user.dob = this.typesUtilsService.getDateFromString(this.user.dateOfBbirth);
		this.customerForm = this.fb.group({
			username: [this.user.username, Validators.nullValidator],
			displayName: [this.user.displayName, Validators.nullValidator],
			firstName: [this.user.firstName, Validators.required],
			lastName: [this.user.lastName, Validators.required],
			middleName: [this.user.middleName, Validators.nullValidator],
			gender: [this.user.gender, Validators.nullValidator],
			email: [this.user.email, [Validators.required, Validators.email]],
			url: [this.user.url, Validators.nullValidator],
			description: [this.user.description, Validators.nullValidator],
			phone: [this.user.phone, Validators.nullValidator],
			language: [this.user.language, Validators.nullValidator],
			password: [this.user.password, Validators.nullValidator],
			avatarUrl: [this.user.avatarUrl, Validators.nullValidator],
			sphere: [this.user.sphere, Validators.nullValidator],
			company: [this.user.company, Validators.nullValidator],
			position: [this.user.position, Validators.nullValidator],
			status: [this.user.status.toString(), Validators.required]

		// user.registeredDate = this.user.registeredDate;
		// user.roles = this.user.roles = [];
		// user.password = this.user.password;
		// user.avatarUrl = this.user.avatarUrl;
		// user.sphere = this.user.sphere;
		// user.company = this.user.company;
		// user.position = this.user.position;
		// user.rank = this.user.rank;
		// user.additionalTel = this.user.additionalTel;
		// user.additionalMail = this.user.additionalMail;
		// user.meta = this.user.meta;
		// user.status = this.user.status;
			//ipAddress: [this.user.ipAddress, Validators.required],
			//type: [this.user.type.toString(), Validators.required]
		});
	}

	/** UI */
	getTitle(): string {
		if (this.user.id > '') {
			return `Edit user '${this.user.firstName} ${
				this.user.lastName
			}'`;
		}

		return 'New user';
	}

	isControlInvalid(controlName: string): boolean {
		const control = this.customerForm.controls[controlName];
		const result = control.invalid && control.touched;
		return result;
	}

	/** ACTIONS */
	prepareUser(): User {
		const controls = this.customerForm.controls;
		const _user = new User();
		_user.id = this.user.id;
		_user.username = controls['username'].value;
		_user.displayName = controls['displayName'].value;
		_user.firstName = controls['firstName'].value;
		_user.lastName = controls['lastName'].value;
		_user.middleName = controls['middleName'].value;
		_user.gender = controls['gender'].value;
		_user.email = controls['email'].value;
		_user.url = controls['url'].value;
		_user.description = controls['description'].value;
		_user.phone = controls['phone'].value;
		_user.language = this.user.language;
		_user.registeredDate = this.user.registeredDate;
		_user.roles = this.user.roles;
		_user.password = controls['password'].value;
		_user.avatarUrl = controls['avatarUrl'].value;
		_user.sphere = controls['sphere'].value;
		_user.company = controls['company'].value;
		_user.position = controls['position'].value;
		_user.rank = this.user.rank;
		_user.additionalTel = this.user.additionalTel;
		_user.additionalMail = this.user.additionalMail;
		_user.meta = this.user.meta;
		_user.status = +controls['status'].value;
		// _user.dateOfBbirth = this.typesUtilsService.dateCustomFormat(controls['dob'].value);

		return _user;
	}

	onSubmit() {
		this.hasFormErrors = false;
		this.loadingAfterSubmit = false;
		const controls = this.customerForm.controls;
		/** check form */
		if (this.customerForm.invalid) {
			Object.keys(controls).forEach(controlName =>
				controls[controlName].markAsTouched()
			);

			this.hasFormErrors = true;
			return;
		}

		const x = this.prepareUser();

		if (x.id > '0') {
			var u: any = diff(this.user, x);
			u.id = x.id;
			this.updateUser(u as UpdateUser);
		} else {
			const c: any = diff(this.user.clear(), x);
			this.createUser(c as CreateUser);
		}
	}

	updateUser(x: UpdateUser) {
		this.loadingAfterSubmit = true;
		this.viewLoading = true;
		this.customerService.updateUser(x).subscribe(res => {
			this.dialogRef.close({
				_user: x,
				isEdit: true
			});
		});
	}

	createUser(x: CreateUser) {
		this.loadingAfterSubmit = true;
		this.viewLoading = true;
		this.customerService.createUser(x).subscribe(res => {
			this.viewLoading = false;
			this.dialogRef.close({
				_user: x,
				isEdit: false
			});
		});
	}

	onAlertClose($event) {
		this.hasFormErrors = false;
	}
}
