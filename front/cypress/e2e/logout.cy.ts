describe('Logout Spec', () => {
    it('should logout the user and redirect to login', () => {
        cy.login();

        cy.contains('span', 'Logout').should('be.visible');
        cy.contains('span', 'Logout').click();

        cy.url().should('include', '/');
    });
});
