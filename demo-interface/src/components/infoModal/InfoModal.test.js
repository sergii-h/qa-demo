import {act, render, screen} from '@testing-library/react';
import * as React from 'react';
import {jest} from '@jest/globals'
import {itemResponse, mockGetItem} from '../../mocks/mockFetch';
import {InfoModal} from "./infoModal";

let fetchSpy;

beforeEach(() => {
    itemResponse.body = {
        id: '1',
        name: 'name1',
        description: 'description1',
        amount: '1',
    }

    fetchSpy = jest.spyOn(window, 'fetch').mockImplementation(mockGetItem);
});

it('should render with mocked values', async () => {
    // when
    await act(() => render(<InfoModal itemId={itemResponse.body.id} />))

    // then
    expect(fetchSpy).toHaveBeenCalledTimes(1)

    expect(screen.getByText(itemResponse.body.name)).toBeVisible()
    expect(screen.getByText(itemResponse.body.amount + ' €')).toBeVisible()
    expect(screen.getByText(itemResponse.body.description)).toBeVisible()
});

it('should render default name if absent in response', () => {
    // given
    itemResponse.changeBody = {
        id: '1',
        description: 'description1',
        amount: '1',
    }

    // when
    act(() => render(<InfoModal itemId={itemResponse.body.id} />))

    // then
    expect(screen.getByText('Info')).toBeVisible()
});
