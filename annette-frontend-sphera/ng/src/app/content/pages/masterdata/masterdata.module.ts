import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormEditComponent} from './forms/form-edit/form-edit.component';
import {RouterModule, Routes} from "@angular/router";
import {FlexLayoutModule} from "@angular/flex-layout";
import {FormsModule} from "@angular/forms";
import {
	MatButtonModule,
	MatCardModule,
	MatCheckboxModule, MatGridListModule,
	MatIconModule,
	MatMenuModule,
	MatSelectModule,
	MatToolbarModule
} from "@angular/material";
import {MaterialDesignFrameworkModule} from "angular6-json-schema-form";

const routes: Routes = [
	{
		path: '',
		component: FormEditComponent,
		children: [
			{
				path: '',
				redirectTo: 'forms',
				pathMatch: 'full'
			},
			{
				path: 'forms',
				component: FormEditComponent
			}
		]
	}
];

@NgModule({
	imports: [
		CommonModule,
		RouterModule.forChild(routes),
		FlexLayoutModule,
		FormsModule,
		MatButtonModule,
		MatCardModule,
		MatCheckboxModule,
		MatIconModule,
		MatMenuModule,
		MatSelectModule,
		MatToolbarModule,
		MatGridListModule,
		MaterialDesignFrameworkModule
	],
	declarations: [FormEditComponent]
})
export class MasterdataModule { }
