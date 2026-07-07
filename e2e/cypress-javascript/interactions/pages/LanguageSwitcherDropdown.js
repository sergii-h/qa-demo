class LanguageSwitcherDropdown {
  get dropdown() {
    return cy.get('[data-testid="language-switcher"]');
  }

  get dropdownValue() {
    return this.dropdown.find('span.p-dropdown-label');
  }

  get items() {
    return cy.get('.p-dropdown-item');
  }
}

module.exports = { LanguageSwitcherDropdown };
