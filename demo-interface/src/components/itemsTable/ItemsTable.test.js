import {act, render, screen, within} from '@testing-library/react'
import * as React from 'react'
import {jest} from '@jest/globals'
import {itemResponse, itemsResponse, mockFetch} from '../../mocks/mockFetch'
import {ItemsTable} from "./itemsTable"
import {BE_API} from "../../helpers";

let fetchSpy

beforeEach(() => {
    fetchSpy = jest.spyOn(window, 'fetch').mockImplementation(mockFetch)
});

it('should render with mocked values', async () => {
    // given
    itemsResponse.body = [{
        id: '1',
        name: 'name1',
        description: 'description1',
        amount: '1',
    }]

    // when
    await act(() => render(<ItemsTable />))

    // then
    expect(fetchSpy).toHaveBeenCalledTimes(1)

    const columnHeaders = screen.getAllByRole('columnheader')
    const cells = screen.getAllByRole('cell')

    expect(columnHeaders).toHaveLength(2)
    expect(columnHeaders[0]).toHaveTextContent('Name')
    expect(columnHeaders[1]).toHaveTextContent('Actions')

    expect(cells).toHaveLength(2)
    expect(cells[0]).toHaveTextContent('name1')
    expect(within(cells[1]).getByText('Info')).toBeVisible()
    expect(within(cells[1]).getByText('Edit')).toBeVisible()
    expect(within(cells[1]).getByText('Delete')).toBeVisible()
});

it('should render CreateModal when clicked', async () => {
    // given
    await act(() => render(<ItemsTable />))

    // when
    await act(() => {
        screen
            .getByLabelText('Create item')
            .click()
    })

    // then
    expect(screen.getByText('New item')).toBeVisible()
});

it('should render Info when clicked', async () => {
    // given
    itemResponse.body = {
        id: '1',
        name: 'name1',
        description: 'description1',
        amount: '1',
    }

    await act(() => render(<ItemsTable />))

    // when
    await act(() => within(screen.getAllByRole('cell')[1]).getByText('Info').click())

    // then
    const infoModal = screen.getByRole('dialog')

    expect(within(infoModal).getByText(itemResponse.body.name)).toBeVisible()
    expect(within(infoModal).getByText(itemResponse.body.amount + ' €')).toBeVisible()
    expect(within(infoModal).getByText(itemResponse.body.description)).toBeVisible()
});

it('should render Edit when clicked', async () => {
    // given
    itemResponse.body = {
        id: '1',
        name: 'name1',
        description: 'description1',
        amount: '1',
    }

    await act(() => render(<ItemsTable />))

    // when
    await act(() => within(screen.getAllByRole('cell')[1]).getByText('Edit').click())

    // then
    expect(screen.getByText('Edit ' + itemResponse.body.name)).toBeVisible()
    expect(screen.getByLabelText('Name')).toHaveProperty('value', itemResponse.body.name)
    expect(screen.getByLabelText('Amount')).toHaveProperty('value', itemResponse.body.amount)
    expect(screen.getByLabelText('Description')).toHaveProperty('value', itemResponse.body.description)
});

it('should remove row when deleted', async () => {
    // given
    itemsResponse.body = [{
        id: '1',
        name: 'name1',
        description: 'description1',
        amount: '1',
    }]

    await act(() => render(<ItemsTable />))

    // when
    await act(() => within(screen.getAllByRole('cell')[1]).getByText('Delete').click())

    // then
    const cells = screen.getAllByRole('cell')

    expect(cells).toHaveLength(1)
    expect(cells[0]).toHaveTextContent('No results found')
});

it('should send Delete request when deleted', async () => {
    // given
    itemsResponse.body = [{
        id: '1',
        name: 'name1',
        description: 'description1',
        amount: '1',
    }]

    await act(() => render(<ItemsTable />))

    // when
    await act(() => within(screen.getAllByRole('cell')[1]).getByText('Delete').click())

    // then
    expect(fetchSpy).toHaveBeenCalledTimes(2)
    expect(fetchSpy).toHaveBeenLastCalledWith(
        `${BE_API}/item/1`,
        expect.objectContaining({"method": "DELETE"})
    )
});
