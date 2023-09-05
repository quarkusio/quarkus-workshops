/**
 * Fight API
 * This API allows a hero and a villain to fight
 *
 * OpenAPI spec version: 1.0
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 *//* tslint:disable:no-unused-variable member-ordering */

import {EventEmitter, Inject, Injectable, Optional, Output} from '@angular/core';
import {HttpClient, HttpEvent, HttpHeaders, HttpResponse} from '@angular/common/http';

import {Observable} from 'rxjs';

import {Fight} from '../model/fight';
import {Fighters} from '../model/fighters';
import {ModelLong} from '../model/modelLong';
import {ModelString} from '../model/modelString';
import {URI} from '../model/uRI';

import {BASE_PATH} from '../variables';
import {Configuration} from '../configuration';

@Injectable()
export class FightService {

  protected basePath = (window as any).NG_CONFIG.API_BASE_URL;
  protected calculateApiBaseUrl = (window as any).NG_CONFIG.CALCULATE_API_BASE_URL;
  public defaultHeaders = new HttpHeaders();
  public configuration = new Configuration();

  @Output() emitter = new EventEmitter<Fight>();
  @Output() narrationEmitter = new EventEmitter<JSON>();

  constructor(protected httpClient: HttpClient, @Optional() @Inject(BASE_PATH) basePath: string, @Optional() configuration: Configuration) {
    if (basePath) {
      this.basePath = basePath;
    }

    if (configuration) {
      this.configuration = configuration;
      this.basePath = basePath || configuration.basePath || this.basePath;
    }

    if (this.calculateApiBaseUrl) {
      // If calculateApiBaseUrl then just replace "ui-super-heroes" with "rest-fights" in the current URL
      this.basePath = window.location.protocol + "//" + window.location.host.replace('ui-super-heroes', 'rest-fights');
    }

    // Fallback to whatever is in the browser if basePath isn't set
    if (!this.basePath) {
      this.basePath = window.location.protocol + "//" + window.location.host;
    }
  }

  /**
   * @param consumes string[] mime-types
   * @return true: consumes contains 'multipart/form-data', false: otherwise
   */
  private canConsumeForm(consumes: string[]): boolean {
    const form = 'multipart/form-data';
    for (const consume of consumes) {
      if (form === consume) {
        return true;
      }
    }
    return false;
  }


  /**
   * Returns all the fights from the database
   *
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  // tag::adocService[]
  public apiFightsGet(observe?: 'body', reportProgress?: boolean): Observable<Array<Fight>>;
  public apiFightsGet(observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<Array<Fight>>>;
  public apiFightsGet(observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<Array<Fight>>>;

  // end::adocService[]
  public apiFightsGet(observe: any = 'body', reportProgress: boolean = false): Observable<any> {

    let headers = this.defaultHeaders;

    // to determine the Accept header
    let httpHeaderAccepts: string[] = [
      'application/json'
    ];
    const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
    if (httpHeaderAcceptSelected != undefined) {
      headers = headers.set('Accept', httpHeaderAcceptSelected);
    }

    // to determine the Content-Type header
    const consumes: string[] = [];

    return this.httpClient.get<Array<Fight>>(`${this.basePath}/api/fights`,
      {
        withCredentials: this.configuration.withCredentials,
        headers: headers,
        observe: observe,
        reportProgress: reportProgress
      }
    );
  }

  /**
   *
   *
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public apiFightsHelloGet(observe?: 'body', reportProgress?: boolean): Observable<ModelString>;
  public apiFightsHelloGet(observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<ModelString>>;
  public apiFightsHelloGet(observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<ModelString>>;
  public apiFightsHelloGet(observe: any = 'body', reportProgress: boolean = false): Observable<any> {

    let headers = this.defaultHeaders;

    // to determine the Accept header
    let httpHeaderAccepts: string[] = [
      'text/plain'
    ];
    const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
    if (httpHeaderAcceptSelected != undefined) {
      headers = headers.set('Accept', httpHeaderAcceptSelected);
    }

    // to determine the Content-Type header
    const consumes: string[] = [];

    return this.httpClient.get<ModelString>(`${this.basePath}/api/fights/hello`,
      {
        withCredentials: this.configuration.withCredentials,
        headers: headers,
        observe: observe,
        reportProgress: reportProgress
      }
    );
  }

  /**
   * Returns a fight for a given identifier
   *
   * @param id Fight identifier
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public apiFightsIdGet(id: ModelLong, observe?: 'body', reportProgress?: boolean): Observable<Fight>;
  public apiFightsIdGet(id: ModelLong, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<Fight>>;
  public apiFightsIdGet(id: ModelLong, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<Fight>>;
  public apiFightsIdGet(id: ModelLong, observe: any = 'body', reportProgress: boolean = false): Observable<any> {

    if (id === null || id === undefined) {
      throw new Error('Required parameter id was null or undefined when calling apiFightsIdGet.');
    }

    let headers = this.defaultHeaders;

    // to determine the Accept header
    let httpHeaderAccepts: string[] = [
      'application/json'
    ];
    const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
    if (httpHeaderAcceptSelected != undefined) {
      headers = headers.set('Accept', httpHeaderAcceptSelected);
    }

    // to determine the Content-Type header
    const consumes: string[] = [];

    return this.httpClient.get<Fight>(`${this.basePath}/api/fights/${encodeURIComponent(String(id))}`,
      {
        withCredentials: this.configuration.withCredentials,
        headers: headers,
        observe: observe,
        reportProgress: reportProgress
      }
    );
  }

  public onNewFight(fight: Fight) {
    this.emitter.emit(fight);
  }

  public onNewFightNarration(narration: JSON) {
    this.narrationEmitter.emit(narration);
  }

  /**
   * Creates a fight between two fighters
   *
   * @param body The two fighters fighting
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public apiFightsPost(body: Fighters, observe?: 'body', reportProgress?: boolean): Observable<Fight>;
  public apiFightsPost(body: Fighters, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<URI>>;
  public apiFightsPost(body: Fighters, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<URI>>;
  public apiFightsPost(body: Fighters, observe: any = 'body', reportProgress: boolean = false): Observable<any> {

    if (body === null || body === undefined) {
      throw new Error('Required parameter body was null or undefined when calling apiFightsPost.');
    }

    let headers = this.defaultHeaders;

    // to determine the Accept header
    let httpHeaderAccepts: string[] = [
      'application/json'
    ];
    const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
    if (httpHeaderAcceptSelected != undefined) {
      headers = headers.set('Accept', httpHeaderAcceptSelected);
    }

    // to determine the Content-Type header
    const consumes: string[] = [
      'application/json'
    ];
    const httpContentTypeSelected: string | undefined = this.configuration.selectHeaderContentType(consumes);
    if (httpContentTypeSelected != undefined) {
      headers = headers.set('Content-Type', httpContentTypeSelected);
    }

    return this.httpClient.post<URI>(`${this.basePath}/api/fights`,
      body,
      {
        withCredentials: this.configuration.withCredentials,
        headers: headers,
        observe: observe,
        reportProgress: reportProgress
      }
    );
  }

  /**
   * Returns two random fighters
   *
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  // tag::adocService[]
  public apiFightsRandomfightersGet(observe?: 'body', reportProgress?: boolean): Observable<Fighters>;
  public apiFightsRandomfightersGet(observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<Fighters>>;
  public apiFightsRandomfightersGet(observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<Fighters>>;
  // end::adocService[]
  public apiFightsRandomfightersGet(observe: any = 'body', reportProgress: boolean = false): Observable<any> {

    let headers = this.defaultHeaders;

    // to determine the Accept header
    let httpHeaderAccepts: string[] = [
      'application/json'
    ];
    const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
    if (httpHeaderAcceptSelected != undefined) {
      headers = headers.set('Accept', httpHeaderAcceptSelected);
    }

    // to determine the Content-Type header
    const consumes: string[] = [];

    return this.httpClient.get<Fighters>(`${this.basePath}/api/fights/randomfighters`,
      {
        withCredentials: this.configuration.withCredentials,
        headers: headers,
        observe: observe,
        reportProgress: reportProgress
      }
    );
  }

  /**
   * Creates a fight between two fighters
   *
   * @param body The two fighters fighting
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public apiNarrateFightPost(body: Fight, observe?: 'body', reportProgress?: boolean): Observable<JSON>;
  public apiNarrateFightPost(body: Fight, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<URI>>;
  public apiNarrateFightPost(body: Fight, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<URI>>;
  public apiNarrateFightPost(body: Fight, observe: any = 'body', reportProgress: boolean = false): Observable<any> {

    if (body === null || body === undefined) {
      throw new Error('Required parameter body was null or undefined when calling apiNarrateFightPost.');
    }

    let headers = this.defaultHeaders;

    // to determine the Accept header
    let httpHeaderAccepts: string[] = [
        'application/json'
    ];
    const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
    if (httpHeaderAcceptSelected != undefined) {
      headers = headers.set('Accept', httpHeaderAcceptSelected);
    }

    // to determine the Content-Type header
    const consumes: string[] = [
      'application/json'
    ];
    const httpContentTypeSelected: string | undefined = this.configuration.selectHeaderContentType(consumes);
    if (httpContentTypeSelected != undefined) {
      headers = headers.set('Content-Type', httpContentTypeSelected);
    }

    return this.httpClient.post<URI>(`${this.basePath}/api/fights/narrate`,
      body,
      {
        withCredentials: this.configuration.withCredentials,
        headers: headers,
        observe: observe,
        reportProgress: reportProgress,
      }
    );
  }
}
