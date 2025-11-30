import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { HouseholdMember } from '../model/householdMember';
import { ShowExpendituresComponent } from './showExpenditures.component';
import { HouseholdMemberService } from '../service/householdMember.service';
import { of } from 'rxjs';

describe('ShowExpendituresComponent', () => {
  let component: ShowExpendituresComponent;
  let fixture: ComponentFixture<ShowExpendituresComponent>;

  beforeEach(async () => {
    // Mock history.state
    spyOnProperty(window.history, 'state', 'get').and.returnValue({ id: 1 });

    await TestBed.configureTestingModule({
      imports: [ShowExpendituresComponent, HttpClientTestingModule],
      providers: [HouseholdMemberService]
    }).compileComponents();
    
    fixture = TestBed.createComponent(ShowExpendituresComponent);
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
    expect(compiled.querySelector('h2')?.textContent).toContain('Übersicht über die Ausgaben');
  });

  // Use spying and mocking for householdMember
  it('should get the householdMembers', () => {
    var member = component.householdMember;
    expect(member.id).toBe(1);
  });

});