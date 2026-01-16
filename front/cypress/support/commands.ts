// ***********************************************
// This example namespace declaration will help
// with Intellisense and code completion in your
// IDE or Text Editor.
// ***********************************************
// declare namespace Cypress {
//   interface Chainable<Subject = any> {
//     customCommand(param: any): typeof customCommand;
//   }
// }
//
// function customCommand(param: any): void {
//   console.warn(param);
// }
//
// NOTE: You can use it like so:
// Cypress.Commands.add('customCommand', customCommand);
//
// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add("login", (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })

declare namespace Cypress {
    interface Chainable<Subject = any> {
        login(admin?: boolean): Chainable<void>;
    }
}

Cypress.Commands.add('login', (admin = true) => {
    cy.visit('/login');

    cy.intercept('POST', '/api/auth/login', {
        statusCode: 200,
        body: {
            token: 'fake-jwt-token',
            type: 'Bearer',
            id: admin ? 1 : 2,
            username: 'userName',
            firstName: 'firstName',
            lastName: 'lastName',
            admin: admin,
        },
    }).as('login');

    cy.get('input[formControlName=email]').type(
        admin ? 'yoga@studio.com' : 'hugo@studio.com'
    );
    cy.get('input[formControlName=password]').type(
        admin ? `${'test!1234'}` : `${'password'}`
    );

    cy.get('button[type=submit]').click();

    cy.wait('@login');

    cy.url().should('include', '/sessions');
});
