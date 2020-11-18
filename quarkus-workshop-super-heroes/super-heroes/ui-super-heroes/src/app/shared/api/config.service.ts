import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../../environments/environment'

const DEFAULT_BASE_PATH = 'http://localhost:8082';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  private _basePath: BehaviorSubject<string> = new BehaviorSubject(null);
  public readonly basePath: Observable<string> = this._basePath.asObservable();

  constructor(private http: HttpClient) {
    console.log(`ConfigService constructor: API_BASE_PATH_LOCAL = ${environment.API_BASE_PATH_LOCAL}`);
    this.http.get(environment.API_BASE_PATH_LOCAL, { responseType: 'text' })
      .subscribe(
        (result: any) => {
          if (result) {
            this._basePath.next(result);
          }
          else {
            console.error('ConfigService: Empty answer!!!');
          }
        },
        error => {
          console.error(error);
          this._basePath.next(DEFAULT_BASE_PATH);
        }
      );
  }
}