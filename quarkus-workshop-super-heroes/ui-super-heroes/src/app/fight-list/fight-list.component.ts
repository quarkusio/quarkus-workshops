import { Component, OnInit } from '@angular/core';
import { Fight, FightService } from '../shared';

@Component({
  selector: 'hero-fight-list',
  templateUrl: './fight-list.component.html',
  styles: []
})
export class FightListComponent implements OnInit {

  fights: Fight[];
  displayedColumns: string[] = ['id', 'fightDate', 'winnerName', 'loserName'];

  constructor(private fightService: FightService) {
  }

  ngOnInit() {
    this.fightService.apiFightsGet().subscribe(fights => this.fights = fights);
  }
}
