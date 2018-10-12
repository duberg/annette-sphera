import {BaseModel} from "./_base.model";

export class SessionModel extends BaseModel {
	id: string;
	userId: string;
	tenantId: string;
	applicationId: string;
	languageId: string;
	startTimestamp: string;
	lastOpTimestamp: string;
	rememberMe: boolean;
	ip: string;
	timestamp: string;

	clear() {
		this.userId = '';
		this.tenantId = '';
		this.applicationId = '';
		this.languageId = '';
		this.startTimestamp = '';
		this.lastOpTimestamp = '';
		this.ip = '';
		this.timestamp = '';
	}
}
