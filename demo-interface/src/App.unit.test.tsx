import { render, screen } from '@testing-library/react';
import App from './App';
import * as services from './services';

describe('App', () => {
  beforeEach(() => {
    vi.spyOn(services, 'getTasks').mockResolvedValue([]);
  });

  it('renders without crashing', () => {
    render(<App />);
    expect(screen.getByRole('button', { name: /create task/i })).toBeInTheDocument();
  });
});

