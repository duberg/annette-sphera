import {BaseModel} from "../../../components/users/_core/models/_base.model";

export class PermissionModel extends BaseModel {
	id: string;
	accessPath: string;
	action: string;

	clear() {
		this.accessPath = '';
		this.action = '';
	}
}
