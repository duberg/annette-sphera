import { NgModule } from '@angular/core';
import { CommonModule,  } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { TranslateModule } from '@ngx-translate/core';
import { PartialsModule } from '../../../partials/partials.module';
import { UsersComponent } from './users.component';
import { HttpClientInMemoryWebApiModule } from 'angular-in-memory-web-api';
// Core
//import { FakeApiService } from '../apps/e-commerce/_core/_server/fake-api.service';
// Core => Services
//import { UsersService } from './_core/services/users.service';
import { UsersService } from '../../../../core/services/users.service';

// Core => Utils
import { HttpUtilsService } from './_core/utils/http-utils.service';
//import { TypesUtilsService } from '../apps/e-commerce/_core/utils/types-utils.service';
//import { LayoutUtilsService } from '../apps/e-commerce/_core/utils/layout-utils.service';
// Shared
//import { ActionNotificationComponent } from '../apps/e-commerce/_shared/action-natification/action-notification.component';
//import { DeleteEntityDialogComponent } from '../apps/e-commerce/_shared/delete-entity-dialog/delete-entity-dialog.component';
//import { FetchEntityDialogComponent } from '../apps/e-commerce/_shared/fetch-entity-dialog/fetch-entity-dialog.component';
//import { UpdateStatusDialogComponent } from '../apps/e-commerce/_shared/update-status-dialog/update-status-dialog.component';
//import { AlertComponent } from '../apps/e-commerce/_shared/alert/alert.component';
// Customers
import { UsersListComponent } from './users-list/users-list.component';
//import { UserEditComponent } from './user-edit/user-edit.component';

// Material
import {
	MatInputModule,
	MatPaginatorModule,
	MatProgressSpinnerModule,
	MatSortModule,
	MatTableModule,
	MatSelectModule,
	MatMenuModule,
	MatProgressBarModule,
	MatButtonModule,
	MatCheckboxModule,
	MatDialogModule,
	MatTabsModule,
	MatNativeDateModule,
	MatCardModule,
	MatRadioModule,
	MatIconModule,
	MatDatepickerModule,
	MatAutocompleteModule,
	MAT_DIALOG_DEFAULT_OPTIONS,
	MatSnackBarModule,
	MatTooltipModule
} from '@angular/material';
import {JwtModule} from "@auth0/angular-jwt";
import {TokenStorage} from "../../../../core/auth/token-storage.service";
import {UserEditDialogComponent} from "./user-edit/user-edit-dialog.component";
import {AlertComponent} from "./_shared/alert/alert.component";
import {TypesUtilsService} from "./_core/utils/types-utils.service";

const routes: Routes = [
	{
		path: '',
		component: UsersComponent,
		children: [
			{
				path: '',
				redirectTo: 'users-list',
				pathMatch: 'full'
			},
			{
				path: 'users-list',
				component: UsersListComponent
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
		//HttpClientInMemoryWebApiModule.forFeature(FakeApiService)
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
		UsersService,
		//OrdersService,
		//ProductRemarksService,
		//ProductSpecificationsService,
		//ProductsService,
		//SpecificationsService,
		TypesUtilsService,
		//LayoutUtilsService
	],
	entryComponents: [
		//ActionNotificationComponent,
		UserEditDialogComponent,
		//DeleteEntityDialogComponent,
		//FetchEntityDialogComponent,
		//UpdateStatusDialogComponent
	],
	declarations: [
		UsersComponent,
		// Shared
		//ActionNotificationComponent,
		//DeleteEntityDialogComponent,
		//FetchEntityDialogComponent,
		//UpdateStatusDialogComponent,
		AlertComponent,
		// Customers
		UsersListComponent,
		UserEditDialogComponent
	]
})
export class UsersModule { }
