import { Component, OnInit, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TypesUtilsService } from '../_core/utils/types-utils.service';
import { UserModel } from '../_core/models/user.model';
import {UsersService} from "../../../../../core/services/users.service";

@Component({
	selector: 'm-customers-edit-dialog',
	templateUrl: './user-edit-dialog.component.html',
	// changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserEditDialogComponent implements OnInit {
	user: UserModel;
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
		console.log(this.data.user);
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
			firstName: [this.user.firstName, Validators.required],
			lastName: [this.user.lastName, Validators.required],
			email: [
				this.user.email,
				[Validators.required, Validators.email]
			],
			dob: [this.user.dob, Validators.nullValidator],
			userName: [this.user.userName, Validators.required],
			gender: [this.user.gender, Validators.required],
			ipAddress: [this.user.ipAddress, Validators.required],
			//type: [this.user.type.toString(), Validators.required]
		});
	}

	/** UI */
	getTitle(): string {
		if (this.user.id > '') {
			return `Edit customer '${this.user.firstName} ${
				this.user.lastName
			}'`;
		}

		return 'New customer';
	}

	isControlInvalid(controlName: string): boolean {
		const control = this.customerForm.controls[controlName];
		const result = control.invalid && control.touched;
		return result;
	}

	/** ACTIONS */
	prepareCustomer(): UserModel {
		const controls = this.customerForm.controls;
		const _customer = new UserModel();
		_customer.id = this.user.id;
		_customer.dateOfBbirth = this.typesUtilsService.dateCustomFormat(controls['dob'].value);
		_customer.firstName = controls['firstName'].value;
		_customer.lastName = controls['lastName'].value;
		_customer.email = controls['email'].value;
		_customer.userName = controls['userName'].value;
		_customer.gender = controls['gender'].value;
		_customer.ipAddress = controls['ipAddress'].value;
		_customer.type = +controls['type'].value;
		_customer.status = this.user.status;
		return _customer;
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

		const editedCustomer = this.prepareCustomer();
		if (editedCustomer.id > '0') {
			this.updateCustomer(editedCustomer);
		} else {
			this.createCustomer(editedCustomer);
		}
	}

	updateCustomer(_customer: UserModel) {
		this.loadingAfterSubmit = true;
		this.viewLoading = true;
		this.customerService.updateCustomer(_customer).subscribe(res => {
			/* Server loading imitation. Remove this on real code */
			this.viewLoading = false;
			this.viewLoading = false;
			this.dialogRef.close({
				_customer,
				isEdit: true
			});
		});
	}

	createCustomer(_customer: UserModel) {
		this.loadingAfterSubmit = true;
		this.viewLoading = true;
		this.customerService.createCustomer(_customer).subscribe(res => {
			this.viewLoading = false;
			this.dialogRef.close({
				_customer,
				isEdit: false
			});
		});
	}

	onAlertClose($event) {
		this.hasFormErrors = false;
	}
}
