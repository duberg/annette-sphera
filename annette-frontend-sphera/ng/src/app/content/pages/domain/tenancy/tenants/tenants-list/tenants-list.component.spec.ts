import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TenantsListComponent } from './tenants-list.component';

describe('TenantsListComponent', () => {
  let component: TenantsListComponent;
  let fixture: ComponentFixture<TenantsListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TenantsListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TenantsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
