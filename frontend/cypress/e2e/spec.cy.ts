// Please make sure, that you're running the postman-collection "Create HouseholdMember Collection (NEW)" first

describe('My First Test', () => {
  it('Visits the initial project page', () => {
    cy.visit('/');

    // Page is reachable 
    cy.contains('CASH-FLOW-RECORDER');

    // Load the User-Overview
    cy.contains('AKTIONSMENÜ FÜR MITGLIEDER').click();

    // Check the new route
    cy.url().should('include', '/home-component');

    // Page hat new Subtitle
    cy.contains('Aktionsmenü für Mitglieder');

    // Load showExpenditure-component from the first user
    cy.get('.getExpenditures').eq(0).click();
    cy.wait(1000);

    // Check the new route
    cy.url().should('include', '/showExpenditures-component');

    // Page hat new Subtitle
    cy.contains('Übersicht über die Ausgaben');

    // Load expenditure-component from the expenditure
    cy.get('.update').eq(0).click();
    cy.wait(1000);

    // Check the new route
    cy.url().should('include', '/expenditure-component');

    // Page hat new Subtitle
    cy.contains('Ausgabe speichern');

    // Select new expenditureCategory for the expenditure
    cy.get('#expenditureCategory').select('1 - Lebensmittel');

    // Save changes for the expenditure
    cy.get('#submit').click();
    cy.wait(1000);

    // Check the new route
    cy.url().should('include', '/home-component');

    // Page hat new Subtitle
    cy.contains('Aktionsmenü für Mitglieder');

    // Load showExpenditure-component from the first user
    cy.get('.getExpenditures').eq(0).click();
    cy.wait(1000);

    // Check the new route
    cy.url().should('include', '/showExpenditures-component');

    // Page hat new Subtitle
    cy.contains('Übersicht über die Ausgaben');

    // Load expenditure-component from the expenditure
    cy.get('.update').eq(3).click();
    cy.wait(1000);

    // Check the new route
    cy.url().should('include', '/expenditure-component');

    // Page hat new Subtitle
    cy.contains('Ausgabe speichern');

    // Check new expenditureCategory for the expenditure
    cy.get('#expenditureCategory').should('have.value', '4');
  })
})
