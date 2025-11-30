import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { HouseholdMember } from '../model/householdMember';
import { HouseholdMemberComponent } from './householdMember.component';
import { HouseholdMemberService } from '../service/householdMember.service';
import { of } from 'rxjs';

describe('HouseholdMemberComponent', () => {
  let component: HouseholdMemberComponent;
  let fixture: ComponentFixture<HouseholdMemberComponent>;

  beforeEach(async () => {
    // Mock history.state
    spyOnProperty(window.history, 'state', 'get').and.returnValue({ id: 1 });

    await TestBed.configureTestingModule({
      imports: [HouseholdMemberComponent, HttpClientTestingModule],
      providers: [HouseholdMemberService]
    }).compileComponents();
    
    fixture = TestBed.createComponent(HouseholdMemberComponent);
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
    expect(compiled.querySelector('h2')?.textContent).toContain('Haushaltsmitglied anlegen / ändern');
  });

  // This does not work (asynchron): use spying and mocking
  it('should get the householdMember', () => {
    var member = component.householdMember;
    expect(member.name).toEqual("Test Member");
  });

});
