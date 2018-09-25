import {HttpErrorResponse} from "@angular/common/http";
import {TranslationService} from "../services/translation.service";
import {Injectable} from "@angular/core";

export declare class CoreError  {
	code: string;
	parameters: string[];
}
