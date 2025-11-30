import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { HouseholdMember } from '../model/householdMember';
import { Account } from '../model/account';
import { IncomeComponent } from './income.component';
import { HouseholdMemberService } from '../service/householdMember.service';
import { of } from 'rxjs';
import { Household } from '../model/household';

describe('IncomeComponent', () => {
  let component: IncomeComponent;
  let fixture: ComponentFixture<IncomeComponent>;

  beforeEach(async () => {
    // Mock history.state for householdMember
    spyOnProperty(window.history, 'state', 'get').and.returnValue({ id: 1 });

    await TestBed.configureTestingModule({
      imports: [IncomeComponent, HttpClientTestingModule],
      providers: [HouseholdMemberService]
    }).compileComponents();
    
    fixture = TestBed.createComponent(IncomeComponent);
    component = fixture.componentInstance;

    // This does not work (asynchron): use spying and mocking
    const households: Partial<Household>[] = [
      { id: 1, name: 'Mein Haus'},
      { id: 2, name: 'Meine Wohnung'}
    ];
    component.householdMember = { id: 1, name: 'Test Member', households: households } as HouseholdMember;
              
    // HTTP-Calls mocken
    const householdMemberService = TestBed.inject(HouseholdMemberService);
    const mockAccounts: Partial<Account>[] = [
      { id: 1, name: 'Girokonto', balance: 1000},
      { id: 2, name: 'Sparkonto', balance: 5000}
    ];
    spyOn(householdMemberService, 'getAccounts').and.returnValue(of(mockAccounts as Account[]));

    fixture.detectChanges();
  });

  // Test-Case for component creation
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // Test-Case for checking the page title
  it('should render title', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h2')?.textContent).toContain('Einnahme speichern');
  });

  // Use spying and mocking for householdMember
  it('should get the householdMembers', () => {
    var member = component.householdMember;
    expect(member.id).toBe(1);
  });

  // Use spying and mocking for accounts from householdMember
  it('should get the accounts from householdMember', () => {
    component.getAccounts(); // call method
    fixture.detectChanges(); // refrash view
    expect(component.accounts.length).toBe(2);
  });

  // This does not work (asynchron): use spying and mocking
  it('should get the households from householdMember', () => {
    component.getHouseholds(); // call method
    fixture.detectChanges(); // refrash view
    expect(component.households.length).toBe(2);
  });

});
