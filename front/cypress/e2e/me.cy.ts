describe('User Information Spec', () => {
    it('should display the connected user information as admin and back', () => {
        cy.intercept('GET', '/api/user/1', {
            id: 1,
            email: 'yoga@studio.com',
            firstName: 'Admin',
            lastName: 'Admin',
            admin: true,
            createdAt: '2025-11-24T00:00:00Z',
            updatedAt: '2026-01-10T00:00:00Z'
        }).as('me');

        cy.login();

        cy.get('[routerLink="me"]').click();
        cy.wait('@me');

        cy.contains('User information').should('be.visible');
        cy.contains('Admin ADMIN').should('be.visible');
        cy.contains('yoga@studio.com').should('be.visible');
        cy.contains('You are admin').should('be.visible');
        cy.contains('Create at:').should('be.visible');
        cy.contains('Last update:').should('be.visible');

        // Admin: delete button must not be visible
        cy.get('button[color="warn"]').should('not.exist');

        cy.contains('mat-icon', 'arrow_back').should('be.visible');
        cy.contains('mat-icon', 'arrow_back').click();
        cy.url().should('include', '/sessions');
    });

    it('should display the connected user information as non-admin and delete account', () => {
        cy.intercept('GET', '/api/user/2', {
            id: 2,
            email: 'hugo@studio.com',
            firstName: 'Hugo',
            lastName: 'Lebolide',
            admin: false,
            createdAt: '2025-11-24T00:00:00Z',
            updatedAt: '2026-01-10T00:00:00Z'
        }).as('me');

        cy.login(false);

        cy.get('[routerLink="me"]').click();
        cy.wait('@me');

        cy.contains('User information').should('be.visible');
        cy.contains('Hugo LEBOLIDE').should('be.visible');
        cy.contains('hugo@studio.com').should('be.visible');
        cy.contains('Create at:').should('be.visible');
        cy.contains('Last update:').should('be.visible');

        cy.intercept('DELETE', '/api/user/2', { statusCode: 200 }).as('deleteUser');

        // User: delete button must be visible
        cy.get('button[color="warn"]').should('be.visible');
        cy.get('button[color="warn"]').click();
        cy.wait('@deleteUser');

        // Snack bar confirmation message
        cy.contains('Your account has been deleted !').should('be.visible');

        cy.url().should('include', '/');
    });
});
