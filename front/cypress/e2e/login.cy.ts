describe('Login spec', () => {
  it('should Login successfull', () => {
    // Arrange
    cy.visit('/login');
    
    cy.intercept('POST', '/api/auth/login', {
      body: {
        id: 1,
        username: 'userName',
        firstName: 'firstName',
        lastName: 'lastName',
        admin: true
      },
    }).as('login');
    
    cy.intercept(
      {
        method: 'GET',
        url: '/api/session',
      },
      []).as('session');
    
    // Act
    cy.get('input[formControlName=email]').type("yoga@studio.com");
    cy.get('input[formControlName=password]').type(`${"test!1234"}`);

    cy.get('button[type=submit]').click();
    cy.wait('@login');

    // Assert
    cy.url().should('include', '/sessions');
  });

  it('should display error message on bad credentials', () => {
    // Arrange
    cy.visit('/login');
    
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 401,
      body: { message: 'Bad credentials' }
    }).as('loginError');
    
    // Act
    cy.get('input[formControlName=email]').type("yoga@studio.com");
    cy.get('input[formControlName=password]').type(`${"badpassword"}`);

    cy.get('button[type=submit]').click();
    cy.wait('@loginError');

    cy.contains('An error occurred').should('be.visible');
  });

  it('should mark required fields and keep submit disabled when fields are missing', () => {
    cy.visit('/login');

    cy.get('input[formControlName=email]').type("yoga@studio.com");

    cy.get('input[formControlName=password]').focus().blur();
    cy.get('input[formControlName=password]').should('have.class', 'ng-invalid').and('have.class', 'ng-touched');

    cy.get('button[type=submit]').should('be.disabled');
  });
});