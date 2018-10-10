import {BaseModel} from "../../../components/users/_core/models/_base.model";

export class TenantModel extends BaseModel {
	id: string;
	name: string;
	defaultApplicationId: string;
	applications: string[];
	defaultLanguageId: string;
	languages: string[];

	clear() {
		this.name = '';
		this.defaultApplicationId = '';
		this.applications = [];
		this.defaultLanguageId = '';
		this.languages = [];
	}
}
