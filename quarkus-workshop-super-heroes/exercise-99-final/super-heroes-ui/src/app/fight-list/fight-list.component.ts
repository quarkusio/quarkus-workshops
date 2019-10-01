import { Component, OnInit } from '@angular/core';
import { Fight } from '../shared';

// const ELEMENT_DATA: Fight[] = [
//   {id: 1, winnerName: 'Hydrogen', winnerLevel: 10, winnerPicture: 'H',  loserName: 'Hydrogen', loserLevel: 2, loserPicture: 'H'},
//   {id: 2, winnerName: 'Helium',   winnerLevel: 12, winnerPicture: 'He', loserName: 'Helium',   loserLevel: 4, loserPicture: 'He'},
//   {id: 3, winnerName: 'Lithium',  winnerLevel: 18, winnerPicture: 'Li', loserName: 'Lithium',  loserLevel: 5, loserPicture: 'Li'},
// ];

export interface PeriodicElement {
  id: number;
  winnerName: string;
  loserName: string;
}

const ELEMENT_DATA: PeriodicElement[] = [
  {id: 1,  winnerName: 'Hydrogen', loserName: 'H'},
  {id: 2,  winnerName: 'Helium', loserName: 'He'},
  {id: 3,  winnerName: 'Lithium', loserName: 'Li'},
  {id: 4,  winnerName: 'Lithium', loserName: 'Li'},
  {id: 5,  winnerName: 'Lithium', loserName: 'Li'},
  {id: 6,  winnerName: 'Lithium', loserName: 'Li'},
  {id: 7,  winnerName: 'Lithium', loserName: 'Li'},
];

@Component({
  selector: 'hero-fight-list',
  templateUrl: './fight-list.component.html',
  styles: []
})
export class FightListComponent implements OnInit {

  displayedColumns: string[] = ['id', 'winnerName', 'loserName'];
  dataSource = ELEMENT_DATA;

  // displayedColumns: string[] = ['id', 'winnerName', 'loserName'];
  // fights = ELEMENT_DATA;

  constructor() { }

  ngOnInit() {
  }

}
