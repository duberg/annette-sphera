import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TenancyComponent } from './tenancy.component';

describe('TenancyComponent', () => {
  let component: TenancyComponent;
  let fixture: ComponentFixture<TenancyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TenancyComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TenancyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
