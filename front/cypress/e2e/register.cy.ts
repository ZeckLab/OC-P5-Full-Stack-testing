describe('Register spec', () => {
    it('should register successfully', () => {
        // Arrange
        cy.visit('/register');

        cy.intercept('POST', '/api/auth/register', {
            statusCode: 200,
            body: {
                id: 42,
                username: 'hector@studio.com',
                firstName: 'Hector',
                lastName: 'Lecastor',
                admin: false
            }
        }).as('register');

        // Act
        cy.get('input[formControlName=firstName]').type('Hector');
        cy.get('input[formControlName=lastName]').type('Lecastor');
        cy.get('input[formControlName=email]').type('hector@studio.com');
        cy.get('input[formControlName=password]').type('password');

        cy.get('button[type=submit]').click();
        cy.wait('@register');

        // Assert
        cy.url().should('include', '/login');
    });

    it('should mark required fields and keep submit disabled when fields are missing', () => {
        // Arrange
        cy.visit('/register');

        // Act
        cy.get('input[formControlName=firstName]').type('Hector');

        cy.get('input[formControlName=lastName]').focus().blur()
        cy.get('input[formControlName=email]').focus().blur()
        cy.get('input[formControlName=password]').focus().blur()

        // Assert
        cy.get('input[formControlName=lastName]')
            .should('have.class', 'ng-invalid')
            .and('have.class', 'ng-touched')

        cy.get('input[formControlName=email]')
            .should('have.class', 'ng-invalid')
            .and('have.class', 'ng-touched')

        cy.get('input[formControlName=password]')
            .should('have.class', 'ng-invalid')
            .and('have.class', 'ng-touched')

        cy.get('button[type=submit]').should('be.disabled')
    });
});