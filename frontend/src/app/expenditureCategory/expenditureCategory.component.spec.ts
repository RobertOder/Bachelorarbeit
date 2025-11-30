import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { HouseholdMember } from '../model/householdMember';
import { ExpenditureCategoryComponent } from './expenditureCategory.component';
import { ExpenditureCategory } from '../model/expenditureCategory';
import { HouseholdMemberService } from '../service/householdMember.service';
import { of } from 'rxjs';
import { HouseholdService } from '../service/household.service';

describe('ExpenditureCategoryComponent', () => {
  let component: ExpenditureCategoryComponent;
  let fixture: ComponentFixture<ExpenditureCategoryComponent>;

  beforeEach(async () => {
    // Mock history.state
    spyOnProperty(window.history, 'state', 'get').and.returnValue({ id: 1 });

    await TestBed.configureTestingModule({
      imports: [ExpenditureCategoryComponent, HttpClientTestingModule],
      providers: [HouseholdMemberService]
    }).compileComponents();
    
    fixture = TestBed.createComponent(ExpenditureCategoryComponent);
    component = fixture.componentInstance;

    // Inputs setzen
    component.householdMember = { id: 1, name: 'Test Member' } as HouseholdMember;
    component.expenditureCategory = { id: 1, name: 'Test ExpenditureCategory' } as ExpenditureCategory;
      
    // HTTP-Calls mocken
    const householdMemberService = TestBed.inject(HouseholdMemberService);
    const householdService = TestBed.inject(HouseholdService);
    spyOn(householdMemberService, 'getHouseholds').and.returnValue(of([]));
    spyOn(householdService, 'getExpenditureCategories').and.returnValue(of([]));

    fixture.detectChanges();
  });

  // Test-Case for component creation
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // Test-Case for checking the page title
  it('should render title', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h2')?.textContent).toContain('Ausgaben-Kategorie anlegen / ändern');
  });

  // This does not work (asynchron): use spying and mocking
  //it('should get the expenditureCategory', () => {
  //  var expenditureCategories = component.expenditureCategory;
  //  expect(expenditureCategories.length).toBe(0);
  //});

});
