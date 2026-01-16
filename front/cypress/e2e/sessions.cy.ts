describe('Sessions', () => {
    describe('List', () => {
        it('should display the list of sessions and the Create Button for admin user', () => {
            cy.intercept('GET', '/api/session', { fixture: 'sessions.json' }).as(
                'sessions'
            );

            cy.login();
            cy.wait('@sessions');

            cy.get('.item').should('have.length', 4);
            cy.contains('Yoga débutant').should('be.visible');
            cy.contains('Create').should('be.visible');
        });

        it('should display the list of sessions and not display Create button for non-admin user', () => {
            cy.intercept('GET', '/api/session', { fixture: 'sessions.json' }).as(
                'sessions'
            );

            // login user
            cy.login(false);
            cy.wait('@sessions');

            cy.contains('Yoga débutant').should('be.visible');
            cy.contains('Create').should('not.exist');
        });
    });

    describe('Detail', () => {
        beforeEach(() => {
            cy.intercept('GET', '/api/session*', { fixture: 'sessions.json' }).as(
                'sessions'
            );

            cy.intercept('GET', '/api/session/2*', {
                id: 2,
                name: 'Yoga débutant',
                description: 'Yoga pour les débutants',
                date: '2026-01-03T12:00:00',
                createdAt: '2026-01-01T10:00:00',
                updatedAt: '2026-01-02T10:00:00',
                teacher_id: 2,
                users: [1, 3, 5],
            }).as('detail');

            cy.intercept('GET', '/api/teacher/2*', {
                id: 2,
                firstName: 'Hélène',
                lastName: 'THIERCELIN',
            }).as('teacher');
        });

        it('should display the session detail and delete button for admin user', () => {
            cy.login();

            cy.contains('mat-card', 'Yoga débutant').within(() => {
                cy.contains('button', 'Detail').click();
            });

            cy.wait('@detail');
            cy.wait('@teacher');

            cy.contains(/yoga débutant/i).should('be.visible');
            cy.contains('Hélène THIERCELIN').should('be.visible');
            cy.contains('3 attendees').should('be.visible');
            cy.contains('Yoga pour les débutants').should('be.visible');
            cy.contains('Create at:').should('be.visible');
            cy.contains('Last update:').should('be.visible');

            cy.contains('Delete').should('be.visible');
        });

        it('should display the session detail and click on participate and unparticipate button for non-admin user', () => {
            // Arrange: user id 2, not in the users list
            cy.login(false);

            cy.contains('mat-card', 'Yoga débutant').within(() => {
                cy.contains('button', 'Detail').click();
            });

            cy.wait('@detail');
            cy.wait('@teacher');

            // Act 1: participate
            // User 2 participates -> user added to the list
            cy.intercept('POST', '/api/session/2/participate/2*', {
                statusCode: 200,
            }).as('participate');

            cy.intercept('GET', '/api/session/2*', {
                id: 2,
                name: 'Yoga débutant',
                description: 'Yoga pour les débutants',
                date: '2026-01-03T12:00:00',
                createdAt: '2026-01-01T10:00:00',
                updatedAt: '2026-01-02T10:00:00',
                teacher_id: 2,
                users: [1, 2, 3, 5],
            }).as('detail');

            cy.contains('Participate').should('be.visible');
            cy.contains('Participate').click();
            cy.wait('@participate');
            cy.wait('@detail');

            // Act 2 : unparticipate
            // User 2 unparticipates -> user removed from the list
            cy.intercept('DELETE', '/api/session/2/participate/2*', {
                statusCode: 200,
            }).as('unparticipate');

            // Delete the user id 2
            cy.intercept('GET', '/api/session/2*', {
                id: 2,
                name: 'Yoga débutant',
                description: 'Yoga pour les débutants',
                date: '2026-01-03T12:00:00',
                createdAt: '2026-01-01T10:00:00',
                updatedAt: '2026-01-02T10:00:00',
                teacher_id: 2,
                users: [1, 3, 5],
            }).as('detail');

            cy.contains('Do not participate').should('be.visible');
            cy.contains('Do not participate').click();
            cy.wait('@unparticipate');
            cy.wait('@detail');

            cy.contains('Participate').should('be.visible');
            cy.contains('Delete').should('not.exist');
        });
    });

    describe('Create', () => {
        beforeEach(() => {
            cy.intercept('GET', '/api/session*', { fixture: 'sessions.json' }).as(
                'sessions'
            );
            cy.intercept('GET', '/api/teacher*', {
                body: [
                    { id: 1, firstName: 'Margot', lastName: 'DELAHAYE' },
                    { id: 2, firstName: 'Hélène', lastName: 'THIERCELIN' },
                ],
            }).as('teachers');
        });

        it('should create a new session as admin', () => {
            // Arrange
            cy.login();

            cy.contains('button', 'Create').click();
            cy.wait('@teachers');

            cy.intercept('POST', '/api/session', {
                statusCode: 200,
                body: {
                    id: 99,
                    name: 'Nouveau cours',
                    description: 'Cours test',
                    date: '2026-01-10',
                    teacher_id: 2,
                    users: [],
                },
            }).as('create');

            cy.intercept('GET', '/api/session*', {
                fixture: 'sessions-after-create.json',
            }).as('sessions-after-create');

            // Act
            cy.get('input[formControlName=name]').type('Nouveau cours');
            cy.get('input[formControlName=date]').type('2026-01-10');
            // Select teacher
            cy.get('mat-select[formControlName=teacher_id]').click();
            cy.contains('mat-option', 'Hélène THIERCELIN').click();
            cy.get('textarea[formControlName=description]').type('Cours test');

            cy.get('button[type=submit]').should('not.be.disabled');
            cy.get('button[type=submit]').click();

            // Assert
            cy.wait('@create');
            cy.url().should('include', '/sessions');
            cy.contains('Session created !').should('be.visible');
            cy.contains('Nouveau cours').should('be.visible');
        });

        it('should keep the submit button disabled when form is empty', () => {
            cy.login();

            cy.contains('button', 'Create').click();

            cy.get('input[formControlName=name]').focus().blur();
            cy.get('input[formControlName=date]').focus().blur();
            cy.get('mat-select[formControlName=teacher_id]').focus().blur();
            cy.get('textarea[formControlName=description]').focus().blur();

            // Button should be disabled when form is invalid
            cy.get('button[type=submit]').should('be.disabled');

            cy.get('input[formControlName=name]').should('have.class', 'ng-invalid');
            cy.get('input[formControlName=date]').should('have.class', 'ng-invalid');
            cy.get('textarea[formControlName=description]').should(
                'have.class',
                'ng-invalid'
            );
            cy.get('mat-select[formControlName=teacher_id]').should(
                'have.class',
                'ng-invalid'
            );
        });
    });

    describe('Update', () => {
        beforeEach(() => {
            // sessions after create
            cy.intercept('GET', '/api/session*', {
                fixture: 'sessions-after-create.json',
            }).as('sessions');

            cy.intercept('GET', '/api/session/99*', {
                id: 99,
                name: 'Nouveau cours',
                description: 'Cours test',
                date: '2026-01-10',
                teacher_id: 2,
                users: [],
            }).as('detail');

            cy.intercept('GET', '/api/teacher*', {
                body: [
                    { id: 1, firstName: 'Margot', lastName: 'DELAHAYE' },
                    { id: 2, firstName: 'Hélène', lastName: 'THIERCELIN' },
                ],
            }).as('teachers');
        });

        it('should update a session as admin', () => {
            // Arrange
            cy.login();

            cy.contains('mat-card', 'Nouveau cours').within(() => {
                cy.contains('button', 'Edit').click();
            });

            cy.wait('@detail');
            cy.wait('@teachers');

            cy.intercept('PUT', '/api/session/99', {
                statusCode: 200,
                body: {
                    id: 99,
                    name: 'Cours modifié',
                    description: 'Description modifiée',
                    date: '2026-01-10',
                    teacher_id: 2,
                    users: [],
                },
            }).as('update');

            // Intercept GET after update
            cy.intercept('GET', '/api/session*', {
                fixture: 'sessions-after-update.json',
            }).as('sessions-after-update');

            // Act
            cy.get('input[formControlName=name]').clear().type('Cours modifié');

            cy.get('textarea[formControlName=description]')
                .clear()
                .type('Description modifiée');

            cy.get('button[type=submit]').should('not.be.disabled');
            cy.get('button[type=submit]').click();

            // Assert
            cy.wait('@update');
            cy.url().should('include', '/sessions');
            cy.contains('Session updated !').should('be.visible');

            cy.wait('@sessions-after-update');
            cy.contains('Cours modifié').should('be.visible');
        });

        it('should keep the submit button disabled when a required field is missing', () => {
            cy.login();

            cy.contains('mat-card', 'Nouveau cours')
                .within(() => {
                    cy.contains('button', 'Edit').click();
                });

            cy.wait('@detail');
            cy.wait('@teachers');

            cy.get('input[formControlName=name]').clear();
            cy.get('button[type=submit]').should('be.disabled');
            cy.get('input[formControlName=name]').should('have.class', 'ng-invalid');
        });

    });

    describe('Delete', () => {
        beforeEach(() => {
            cy.intercept('GET', '/api/session*', {
                fixture: 'sessions-after-update.json',
            }).as('sessions');

            cy.intercept('GET', '/api/session/99*', {
                id: 99,
                name: 'Cours modifié',
                description: 'Description modifiée',
                date: '2026-01-10',
                teacher_id: 2,
                users: [],
            }).as('detail');

            cy.intercept('GET', '/api/teacher/2*', {
                id: 2,
                firstName: 'Hélène',
                lastName: 'THIERCELIN',
            }).as('teacher');
        });

        it('should delete a session as admin', () => {
            // Arrange
            cy.login();

            cy.contains('mat-card', 'Cours modifié').within(() => {
                cy.contains('button', 'Detail').click();
            });

            cy.wait('@detail');
            cy.wait('@teacher');

            cy.intercept('DELETE', '/api/session/99', { statusCode: 200 }).as(
                'delete'
            );
            cy.intercept('GET', '/api/session*', { fixture: 'sessions.json' }).as(
                'sessions-after-delete'
            );

            // Act
            cy.contains('button', 'Delete').click();
            cy.wait('@delete');

            // Assert: After deletion, user is redirected to the sessions list
            cy.url().should('include', '/sessions');
            cy.contains('Session deleted !').should('be.visible');

            cy.wait('@sessions-after-delete');
            cy.contains('Cours modifié').should('not.exist');
        });
    });
});
