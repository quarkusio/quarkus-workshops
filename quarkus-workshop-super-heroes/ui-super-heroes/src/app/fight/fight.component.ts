import { Component, OnInit } from '@angular/core';
import { Fighters, FightService, Hero, Villain } from '../shared';

@Component({
  selector: 'hero-fight',
  templateUrl: './fight.component.html'
})
export class FightComponent implements OnInit {

  figthers: Fighters = new Fighters();

  constructor(private fightService: FightService) {
  }

  ngOnInit() {
    this.newFighters();
  }

  fight() {
    this.fightService.apiFightsPost(this.figthers).subscribe();
  }

  newFighters() {
    this.fightService.apiFightsRandomfightersGet().subscribe(figthers => this.figthers = figthers);
  }
}
