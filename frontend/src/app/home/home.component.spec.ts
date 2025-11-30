import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HomeComponent } from './home.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { HouseholdMemberService } from '../service/householdMember.service';
import { HouseholdMember } from '../model/householdMember';

// Unit test for the home component
describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;

  // execute before each test
  beforeEach(async () => {

    await TestBed.configureTestingModule({
      imports: [
        HomeComponent,
        HttpClientTestingModule
      ],
      providers: [HouseholdMemberService]
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;

    const testHouseholdMember: HouseholdMember = new HouseholdMember();
    component.householdMembers = [testHouseholdMember];

    fixture.detectChanges();
  });

  // Test-Case for component creation
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // Test-Case for checking the page title
  it('should have a title', () => {
    var title = component.getTitle();
    expect(title).toContain("Aktionsmenü für Mitglieder");
  });

  // This does not work (asynchron): use spying and mocking
  it('should get the householdMembers', () => {
    var members = component.householdMembers;
    expect(members.length).toBe(1);
  });

  // Test-Case for changing the page title
  it('should render the title in the template', () => {
    component.title = 'This is the home page!';
    fixture.detectChanges(); // Update the DOM
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h2')?.textContent).toContain('This is the home page!');
  });
});