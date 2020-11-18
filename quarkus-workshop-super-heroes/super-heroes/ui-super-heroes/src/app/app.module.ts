import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { FightListComponent } from './fight-list/fight-list.component';
import { FightComponent } from './fight/fight.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatDividerModule } from '@angular/material/divider';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { HttpClientModule } from '@angular/common/http';
import { MatGridListModule } from '@angular/material/grid-list';

import { FightService } from './shared/api/fight.service';
import { ConfigService } from './shared/api/config.service';

@NgModule({
  declarations: [
    AppComponent,
    FightListComponent,
    FightComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    MatDividerModule,
    MatCardModule,
    MatButtonModule,
    MatGridListModule,
    MatTableModule
  ],
  providers: [FightService, ConfigService],
  bootstrap: [AppComponent]
})
export class AppModule { }
