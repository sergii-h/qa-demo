import {act, fireEvent, render, screen, within} from '@testing-library/react'
import * as React from 'react'
import {jest} from '@jest/globals'
import {ItemsTable} from "./itemsTable"
import {BE_API} from "../../helpers";

const MockFetch = require('../../mocks/mockFetch').default;
const spyOnFetch = jest.spyOn(window, 'fetch')

let mockFetch
let fetchSpy

beforeEach(() => {
    mockFetch = new MockFetch()
    fetchSpy = spyOnFetch.mockImplementation(mockFetch.execute)
});

it('should render multiple rows', async () => {
    // given
    mockFetch = new MockFetch({
        itemsResponse: {
            status: 200,
            body: [
                {
                    id: '1',
                    name: 'name1',
                    description: 'description1',
                    amount: '1',
                },
                {
                    id: '2',
                    name: 'name2',
                    description: 'description2',
                    amount: '2',
                },
            ]
        }
    })

    fetchSpy = spyOnFetch.mockImplementation(mockFetch.execute)

    // when
    await act(() => render(<ItemsTable />))

    // then
    expect(fetchSpy).toHaveBeenCalledTimes(1)

    const columnHeaders = screen.getAllByRole('columnheader')
    const cells = screen.getAllByRole('cell')

    expect(columnHeaders).toHaveLength(2)
    expect(columnHeaders[0]).toHaveTextContent('Name')
    expect(columnHeaders[1]).toHaveTextContent('Actions')

    expect(cells).toHaveLength(4)
    expect(cells[0]).toHaveTextContent('name1')
    expect(within(cells[1]).getByText('Info')).toBeVisible()
    expect(within(cells[1]).getByText('Edit')).toBeVisible()
    expect(within(cells[1]).getByText('Delete')).toBeVisible()
    expect(cells[2]).toHaveTextContent('name2')
    expect(within(cells[3]).getByText('Info')).toBeVisible()
    expect(within(cells[3]).getByText('Edit')).toBeVisible()
    expect(within(cells[3]).getByText('Delete')).toBeVisible()
});

it('should render No results when no items received', async () => {
    // given
    mockFetch = new MockFetch({
        itemsResponse: {
            status: 200,
            body: []
        }
    })

    spyOnFetch.mockImplementation(mockFetch.execute)

    // when
    await act(() => render(<ItemsTable />))

    // then
    const cells = screen.getAllByRole('cell')

    expect(cells).toHaveLength(1)
    expect(cells[0]).toHaveTextContent('No results found')
});

it('should render CreateModal when clicked', async () => {
    // given
    await act(() => render(<ItemsTable />))

    // when
    await act(() => screen.getByLabelText('Create item').click())

    // then
    expect(screen.getByText('New item')).toBeVisible()
});

[0, 1].forEach(closeButton => {
    it(`should close CreateModal when Close button ${closeButton} clicked`, async () => {
        // given
        await act(() => render(<ItemsTable />))

        await act(() => screen.getByLabelText('Create item').click())

        // when
        await act(() => screen.getAllByLabelText('Close')[closeButton].click())

        // then
        expect(screen.queryByRole('dialog')).toBeNull()
    })
});

it('should get /items when CreateModal saved', async () => {
    // given
    await act(() => render(<ItemsTable />))
    await act(() => screen.getByLabelText('Create item').click())
    await act(() => fireEvent.change(screen.getByLabelText('Description'), {target: {value: 'description'}}))

    // when
    await act(() =>
        screen
            .getAllByRole('button')
            .find(element => element.className === 'p-button p-component create-button')
            .click()
    )

    // then
    expect(fetchSpy).toHaveBeenNthCalledWith(1,
        `${BE_API}/items`,
        expect.objectContaining({"method": "GET"})
    )
});

it('should not render Spinner when Create clicked', async () => {
    // given
    await act(() => render(<ItemsTable />))

    // when
    await act(() => screen.getByLabelText('Create item').click())

    // then
    expect(document.querySelector('.p-progress-spinner')).toBeNull()
});

it('should render InfoModal when clicked', async () => {
    // given
    mockFetch = new MockFetch({
        itemsResponse: {
            status: 200,
            body: [
                {
                    id: '1',
                    name: 'name1',
                    description: 'description1',
                    amount: '1',
                }
            ]
        },
        itemResponse: {
            status: 200,
            body: {
                id: '1',
                name: 'name1',
                description: 'description1',
                amount: '1',
            }
        }
    })

    spyOnFetch.mockImplementation(mockFetch.execute)

    await act(() => render(<ItemsTable />))

    // when
    await act(() => within(screen.getAllByRole('cell')[1]).getByText('Info').click())

    // then
    expect(within(screen.getByRole('dialog')).getByText(mockFetch.payload.itemResponse.body.name)).toBeVisible()
});

it('should get /items with id when Info clicked', async () => {
    // given
    await act(() => render(<ItemsTable />))

    // when
    await act(() => within(screen.getAllByRole('cell')[1]).getByText('Info').click())

    // then
    expect(fetchSpy).toHaveBeenCalledTimes(3)

    expect(fetchSpy).toHaveBeenCalledWith(
        `${BE_API}/items`,
        expect.objectContaining({"method": "GET"})
    )

    expect(fetchSpy).toHaveBeenCalledWith(
        `${BE_API}/items/${mockFetch.payload.itemResponse.body.id}`,
        expect.objectContaining({"method": "GET"})
    )

    expect(fetchSpy).toHaveBeenCalledWith(
        `${BE_API}/items/isValid/${mockFetch.payload.itemResponse.body.id}`,
        expect.objectContaining({"method": "GET"})
    )
});

it('should close InfoModal when clicked', async () => {
    // given
    await act(() => render(<ItemsTable />))

    // when
    await act(() => within(screen.getAllByRole('cell')[1]).getByText('Info').click())

    // and
    await act(() => screen.getByLabelText('Close').click())

    // then
    expect(screen.queryByRole('dialog')).toBeNull()
});

it('should render EditModal when clicked', async () => {
    // given
    mockFetch = new MockFetch({
        itemsResponse: {
            status: 200,
            body: [
                {
                    id: '1',
                    name: 'name1',
                    description: 'description1',
                    amount: '1',
                }
            ]
        },
        itemResponse: {
            status: 200,
            body: {
                id: '1',
                name: 'name1',
                description: 'description1',
                amount: '1',
            }
        }
    })

    spyOnFetch.mockImplementation(mockFetch.execute)

    await act(() => render(<ItemsTable />))

    // when
    await act(() => within(screen.getAllByRole('cell')[1]).getByText('Edit').click())

    // then
    expect(screen.getByText('Edit ' + mockFetch.payload.itemResponse.body.name)).toBeVisible()
});

it('should get /items with id when Edit clicked', async () => {
    // given
    await act(() => render(<ItemsTable />))

    // when
    await act(() => within(screen.getAllByRole('cell')[1]).getByText('Edit').click())

    // then
    expect(fetchSpy).toHaveBeenCalledTimes(2)

    expect(fetchSpy).toHaveBeenLastCalledWith(
        `${BE_API}/items/${mockFetch.payload.itemResponse.body.id}`,
        expect.objectContaining({"method": "GET"})
    )
});

[0, 1].forEach(closeButton => {
    it(`should close EditModal when Close button ${closeButton} clicked`, async () => {
        // given
        await act(() => render(<ItemsTable />))

        await act(() => within(screen.getAllByRole('cell')[1]).getByText('Edit').click())

        // when
        await act(() => screen.getAllByLabelText('Close')[closeButton].click())

        // then
        expect(screen.queryByRole('dialog')).toBeNull()
    })
});

it('should get /items when EditModal saved', async () => {
    // given
    await act(() => render(<ItemsTable />))
    await act(() => within(screen.getAllByRole('cell')[1]).getByText('Edit').click())

    // when
    await act(() => {
        screen
            .getAllByRole('button')
            .find(element => element.className.includes('save-button'))
            .click()
    })

    // then
    expect(fetchSpy).toHaveBeenNthCalledWith(1,
        `${BE_API}/items`,
        expect.objectContaining({"method": "GET"})
    )
});

it('should remove row when deleted', async () => {
    // given
    mockFetch = new MockFetch({
        itemsResponse: {
            status: 200,
            body: [
                {
                    id: '1',
                    name: 'name1',
                    description: 'description1',
                    amount: '1',
                }
            ]
        },
        itemResponse: {
            status: 200,
            body: {
                id: '1',
                name: 'name1',
                description: 'description1',
                amount: '1',
            }
        }
    })

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
    await act(() => render(<ItemsTable />))

    // when
    await act(() => within(screen.getAllByRole('cell')[1]).getByText('Delete').click())

    // then
    expect(fetchSpy).toHaveBeenCalledTimes(2)

    expect(fetchSpy).toHaveBeenLastCalledWith(
        `${BE_API}/items/${mockFetch.payload.itemResponse.body.id}`,
        expect.objectContaining({"method": "DELETE"})
    )
});
