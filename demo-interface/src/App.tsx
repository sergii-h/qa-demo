import React from 'react';
import TasksTable from './components/tasksTable';
import LanguageSwitcher from './components/languageSwitcher';
import 'primereact/resources/themes/saga-blue/theme.css';
import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';
import 'primeflex/primeflex.css';

function App() {
  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'flex-end', padding: '1rem' }}>
        <LanguageSwitcher />
      </div>
      <TasksTable />
    </div>
  );
}

export default App;
