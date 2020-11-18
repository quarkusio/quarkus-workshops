import { Component, OnInit } from '@angular/core';
import { Fighters, FightService, Hero, Villain } from '../shared';

import { ConfigService } from '../shared/api/config.service';

@Component({
  selector: 'hero-fight',
  templateUrl: './fight.component.html'
})
export class FightComponent implements OnInit {

  figthers: Fighters = new Fighters();
  winner: String;

  constructor(private fightService: FightService, private configService: ConfigService) {
    this.configService.basePath.subscribe(basePath => {
      console.log('basePath', basePath);

      if (basePath) {
        this.newFighters();
      }
     }); 
  }

  ngOnInit() {
    this.newFighters();
  }

  fight() {
    this.fightService.apiFightsPost(this.figthers).subscribe(
      fight => {
        this.fightService.onNewFight(fight);
        this.winner = fight.winnerName;
      }
    );
  }

  newFighters() {
    this.winner = null;
    this.fightService.apiFightsRandomfightersGet().subscribe(figthers => this.figthers = figthers);
  }
}
