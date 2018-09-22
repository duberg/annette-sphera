import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {ApplicationStateStorage} from "../../../../core/services/application-state-storage.service";

@Component({
	selector: 'm-profile',
	templateUrl: './profile.component.html',
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProfileComponent implements OnInit {

	user = 'asdasdsa';

	constructor() {
	}

	ngOnInit() {
	}

}
