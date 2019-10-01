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
    //this.fightService.apiFightsRandomfightersGet().subscribe(figthers => this.figthers = figthers);
    this.figthers.hero = {name: 'Chewbacca', picture: 'https://www.superherodb.com/pictures2/portraits/10/050/10466.jpg', powers: 'Agility, Longevity, Marksmanship, Natural Weapons, Stealth, Super Strength, Weapons Master'};
    this.figthers.villain = {name: 'Egg Fu', picture: 'https://www.superherodb.com/pictures2/portraits/11/050/11157.jpg', powers: 'Cold Resistance, Duplication, Explosion Manipulation, Jump, Natural Weapons, Power Suit, Resurrection, Super Strength, Technopath/Cyberpath, Toxin and Disease Resistance, Weapon-based Powers'};
  }
}
