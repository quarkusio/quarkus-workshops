import { Component } from '@angular/core';

@Component({
  selector: 'hero-root',
  template: `
    <div style="text-align:center" class="content">
      <h1>
        Welcome to {{title}}!
      </h1>
      <hero-fight></hero-fight>
      <hero-fight-list></hero-fight-list>
    </div>
  `,
  styles: []
})
export class AppComponent {
  title = 'Super Heroes Fight';
}
