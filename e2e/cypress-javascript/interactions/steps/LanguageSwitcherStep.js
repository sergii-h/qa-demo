const { step } = require('@/decorators/step-decorator');
const { LanguageSwitcherDropdown } = require('@/interactions/pages/LanguageSwitcherDropdown');

class LanguageSwitcherStep {
  constructor() {
    this.dropdown = new LanguageSwitcherDropdown();
    this.selectLanguage = step("Select language '{language}'", this._selectLanguage.bind(this));
  }

  _selectLanguage(language) {
    this.dropdown.dropdown.click();
    this.dropdown.items.contains(language).click();
    this.dropdown.dropdownValue.should('have.text', language);
  }
}

module.exports = { LanguageSwitcherStep };
