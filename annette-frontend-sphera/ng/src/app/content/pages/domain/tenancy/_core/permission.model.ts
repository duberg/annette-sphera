import {BaseModel} from "./_base.model";

export class PermissionModel extends BaseModel {
	id: string;
	accessPath: string;
	action: string;

	clear() {
		this.accessPath = '';
		this.action = '';
	}
}
