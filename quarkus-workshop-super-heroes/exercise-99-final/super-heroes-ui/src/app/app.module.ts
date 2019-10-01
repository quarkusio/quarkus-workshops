import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { FightListComponent } from './fight-list/fight-list.component';
import { FightComponent } from './fight/fight.component';

@NgModule({
  declarations: [
    AppComponent,
    FightListComponent,
    FightComponent
  ],
  imports: [
    BrowserModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
