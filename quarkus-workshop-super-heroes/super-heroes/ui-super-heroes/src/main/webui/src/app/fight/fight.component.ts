import {Component, OnInit} from '@angular/core';
import {Fighters, FightService} from '../shared';

@Component({
  selector: 'hero-fight',
  templateUrl: './fight.component.html'
})
export class FightComponent implements OnInit {

  fighters: Fighters = new Fighters();
  winner: String;

  constructor(private fightService: FightService) {
  }

  ngOnInit() {
    this.newFighters();
  }

  fight() {
    this.fightService.apiFightsPost(this.fighters).subscribe(
      fight => {
        this.fightService.onNewFight(fight);
        this.winner = fight.winnerName;
      }
    );
  }

  narrate() {
    this.fightService.apiNarrateFightsPost(this.fighters).subscribe(
      narration => {
        this.fightService.onNewFightNarration(narration);
      }
    );
  }

  newFighters() {
    this.winner = null;
    this.fightService.apiFightsRandomfightersGet().subscribe(fighters => this.fighters = fighters);
  }
}
