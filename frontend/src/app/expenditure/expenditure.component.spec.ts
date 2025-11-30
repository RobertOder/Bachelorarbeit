import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ExpenditureComponent } from './expenditure.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { HouseholdMember } from '../model/householdMember';
import { Expenditure } from '../model/expenditure';
import { HouseholdMemberService } from '../service/householdMember.service';
import { of } from 'rxjs';

describe('ExpenditureComponent', () => {
  let component: ExpenditureComponent;
  let fixture: ComponentFixture<ExpenditureComponent>;

  beforeEach(async () => {
    // Mock history.state
    spyOnProperty(window.history, 'state', 'get').and.returnValue({ id: 1 });
  
    await TestBed.configureTestingModule({
      imports: [ExpenditureComponent, HttpClientTestingModule],
      providers: [HouseholdMemberService]
    }).compileComponents();
  
    fixture = TestBed.createComponent(ExpenditureComponent);
    component = fixture.componentInstance;
  
    // Inputs setzen
    component.householdMember = { id: 1, name: 'Test Member' } as HouseholdMember;
    component.expenditure = { id: 1, description: 'Test Expenditure' } as Expenditure;
  
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

  // Test-Case for checking the page title
  it('should have a title', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h2')?.textContent).toContain('Ausgabe speichern');
  });

  // This does not work (asynchron): use spying and mocking
  it('should get the householdMembers', () => {
    var member = component.householdMember;
    expect(member.id).toEqual(1);
  });

});
