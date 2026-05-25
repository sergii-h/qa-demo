import React from 'react';
import TasksTable from './components/tasksTable';
import LanguageSwitcher from './components/languageSwitcher';
import 'primereact/resources/themes/saga-blue/theme.css';
import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';
import 'primeflex/primeflex.css';
import './app.css';

function App() {
  return (
    <div className="app-container">
      <header className="app-header">
        <LanguageSwitcher />
      </header>
      <main className="app-main">
        <TasksTable />
      </main>
    </div>
  );
}

export default App;
