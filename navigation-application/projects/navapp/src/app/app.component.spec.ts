import { HttpClientModule } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ClientAppModule } from './client-app';
import { MainMenuModule } from './main-menu';
import { NavConfigService } from './services';
import { SharedModule } from './shared';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;

  beforeEach(() => {
    fixture = TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        AppRoutingModule,
        MainMenuModule,
        ClientAppModule,
        HttpClientModule,
        SharedModule,
      ],
      declarations: [AppComponent],
      providers: [NavConfigService],
    }).createComponent(AppComponent);

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
  });

  it('should create the app', () => {
    expect(component).toBeDefined();
  });
});
