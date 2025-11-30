import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { HouseholdMember } from '../model/householdMember';
import { Household } from '../model/household';
import { HouseholdComponent } from './household.component';
import { HouseholdMemberService } from '../service/householdMember.service';
import { HouseholdService } from '../service/household.service';
import { of } from 'rxjs';

describe('HouseholdComponent', () => {
  let component: HouseholdComponent;
  let fixture: ComponentFixture<HouseholdComponent>;

  beforeEach(async () => {
    // Mock history.state
    spyOnProperty(window.history, 'state', 'get').and.returnValue({ id: 1 });

    await TestBed.configureTestingModule({
      imports: [HouseholdComponent, HttpClientTestingModule],
      providers: [HouseholdService]
    }).compileComponents();
    
    fixture = TestBed.createComponent(HouseholdComponent);
    component = fixture.componentInstance;

    // Inputs setzen
    component.householdMember = { id: 1, name: 'Test Member' } as HouseholdMember;
          
    // HTTP-Calls mocken
    const householdMemberService = TestBed.inject(HouseholdMemberService);
    spyOn(householdMemberService, 'getAccounts').and.returnValue(of([]));

    fixture.detectChanges();
  });

  // Test-Case for component creation
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // Test-Case for checking the page title
  it('should render title', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h2')?.textContent).toContain('Haushalt anlegen / beitreten');
  });

  // This does not work (asynchron): use spying and mocking
  it('should get the householdMember', () => {
    var member = component.householdMember;
    expect(member.name).toEqual("Test Member");
  });

});
