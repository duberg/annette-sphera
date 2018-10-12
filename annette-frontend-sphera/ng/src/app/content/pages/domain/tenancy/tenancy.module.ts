import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TenancyComponent} from './tenancy.component';
import {RouterModule, Routes} from "@angular/router";
import {RolesListComponent} from "./roles/roles-list/roles-list.component";
import {PermissionsListComponent} from './permissions/permissions-list/permissions-list.component';
import {PermissionEditComponent} from './permissions/permission-edit/permission-edit.component';
import {PermissionsService} from "./_core/services/permissions.service";
import {
	MAT_DIALOG_DEFAULT_OPTIONS,
	MatAutocompleteModule,
	MatButtonModule,
	MatCardModule,
	MatCheckboxModule,
	MatDatepickerModule,
	MatDialogModule,
	MatIconModule,
	MatInputModule,
	MatListModule,
	MatMenuModule,
	MatNativeDateModule,
	MatPaginatorModule,
	MatProgressBarModule,
	MatProgressSpinnerModule,
	MatRadioModule,
	MatSelectModule,
	MatSnackBarModule,
	MatSortModule,
	MatTableModule,
	MatTabsModule,
	MatTooltipModule
} from "@angular/material";
import {PartialsModule} from "../../../partials/partials.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {TranslateModule} from "@ngx-translate/core";
import {HttpUtilsService} from "../../components/users/_core/utils/http-utils.service";
import {TypesUtilsService} from "../../components/users/_core/utils/types-utils.service";
import {TenantsListComponent} from './tenants/tenants-list/tenants-list.component';
import {TenantEditComponent} from './tenants/tenant-edit/tenant-edit.component';
import {RolesEditComponent} from "./roles/role-edit/roles-edit.component";
import {TenantsService} from "./_core/services/tenants.service";
import {SessionsListComponent} from './sessions/sessions-list/sessions-list.component';
import {SessionsService} from "./_core/services/sessions.service";

const routes: Routes = [
	{
		path: '',
		component: TenancyComponent,
		children: [
			{
				path: '',
				redirectTo: 'roles',
				pathMatch: 'full'
			},
			{
				path: 'tenants',
				component: TenantsListComponent
			},
			{
				path: 'roles',
				component: RolesListComponent
			},
			{
				path: 'permissions',
				component: PermissionsListComponent
			},
			{
				path: 'sessions',
				component: SessionsListComponent
			}
		]
	}
];

@NgModule({
  imports: [
	  MatDialogModule,
	  CommonModule,
	  PartialsModule,
	  RouterModule.forChild(routes),
	  FormsModule,
	  ReactiveFormsModule,
	  TranslateModule.forChild(),
	  MatButtonModule,
	  MatMenuModule,
	  MatSelectModule,
	  MatInputModule,
	  MatTableModule,
	  MatAutocompleteModule,
	  MatRadioModule,
	  MatIconModule,
	  MatNativeDateModule,
	  MatProgressBarModule,
	  MatDatepickerModule,
	  MatCardModule,
	  MatPaginatorModule,
	  MatSortModule,
	  MatCheckboxModule,
	  MatProgressSpinnerModule,
	  MatSnackBarModule,
	  MatTabsModule,
	  MatTooltipModule,
	  MatListModule
  ],
	providers: [
		{
			provide: MAT_DIALOG_DEFAULT_OPTIONS,
			useValue: {
				hasBackdrop: true,
				panelClass: 'm-mat-dialog-container__wrapper',
				height: 'auto',
				width: '900px'
			}
		},
		HttpUtilsService,
		TypesUtilsService,
		PermissionsService,
		TenantsService,
		SessionsService
	],
  declarations: [
  	TenancyComponent,
	RolesListComponent,
	RolesEditComponent,
	PermissionsListComponent,
	PermissionEditComponent,
	TenantsListComponent,
	TenantEditComponent,
	SessionsListComponent,
  ]
})
export class TenancyModule { }
