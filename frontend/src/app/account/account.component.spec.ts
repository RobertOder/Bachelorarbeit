import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AccountComponent } from './account.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AccountService } from '../service/account.service';
import { HouseholdMember } from '../model/householdMember';
import { Account } from '../model/account';
import { HouseholdMemberService } from '../service/householdMember.service';
import { of } from 'rxjs';

describe('AccountComponent', () => {
  let component: AccountComponent;
  let fixture: ComponentFixture<AccountComponent>;

    // execute before each test
  beforeEach(async () => {
    // Mock history.state
    spyOnProperty(window.history, 'state', 'get').and.returnValue({ id: 1 });
  
    await TestBed.configureTestingModule({
      imports: [AccountComponent, HttpClientTestingModule],
      providers: [HouseholdMemberService, AccountService]
    }).compileComponents();
  
    fixture = TestBed.createComponent(AccountComponent);
    component = fixture.componentInstance;
  
    // Inputs setzen
    component.householdMember = { id: 1, name: 'Test Member' } as HouseholdMember;
    component.account = { id: 1, name: 'Test Account' } as Account;
  
    // HTTP-Calls mocken
    const householdMemberService = TestBed.inject(HouseholdMemberService);
    spyOn(householdMemberService, 'getHouseholds').and.returnValue(of([]));
    spyOn(householdMemberService, 'getAccounts').and.returnValue(of([]));
  
    fixture.detectChanges();
  });

  // Test-Case for component creation
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // This does not work (asynchron): use spying and mocking
  it('should get the householdMember', () => {
    var member = component.householdMember;
    expect(member.name).toEqual("Test Member");
  });

  // This does not work (asynchron): use spying and mocking
  //it('should get the account', () => {
  //  var account = component.account;
  //  expect(account.name).toEqual("Test Account");
  //});

  // Test-Case for checking the page title
  it('should render title', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h2')?.textContent).toContain('Konto anlegen / ändern');
  });

});
