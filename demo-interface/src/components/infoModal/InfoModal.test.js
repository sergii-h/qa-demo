import {act, render, within} from '@testing-library/react';
import * as React from 'react';
import {jest} from '@jest/globals'
import {InfoModal} from "./infoModal";
import {BE_API} from "../../helpers";

const title = () => within(document.querySelector('.p-dialog-title'))
const amount = () => within(document.querySelector('#amount'))
const description = () => within(document.querySelector('#description'))
const isValid = () => within(document.querySelector('#valid'))

const MockFetch = require('../../mocks/mockFetch').default;
const spyOnFetch = jest.spyOn(window, 'fetch')

let mockFetch
let fetchSpy

beforeEach(() => {
    mockFetch = new MockFetch()
    fetchSpy = spyOnFetch.mockImplementation(mockFetch.execute)
});

[
    { name: "name",         amount: 1,     description: "description",         isValid: true  },
    { name: "name",         amount: 0.5,   description: "description",         isValid: true  },
    { name: "name",         amount: "0,5", description: "description",         isValid: true  },
    { name: "another name", amount: -1,    description: "another description", isValid: true  },
    { name: " ",            amount: 0,     description: " ",                   isValid: true  },
    { name: "name",         amount: "",    description: "",                    isValid: true  },
    { name: "name",         amount: "01",  description: "",                    isValid: false },
].forEach((data) => {
    it(`should render with values { '${data.name}', ${data.amount}, '${data.description}' }`, async () => {
        // given
        mockFetch = new MockFetch({
            itemResponse: {
                status: 200,
                body: {
                    id: '1',
                    name: data.name,
                    description: data.description,
                    amount: data.amount,
                }
            },
            isValidResponse: {
                status: 200,
                body: data.isValid
            }
        })

        spyOnFetch.mockImplementation(mockFetch.execute)

        // when
        await act(() => render(<InfoModal itemId={mockFetch.payload.itemResponse.body.id} />))

        // then
        expect(title().getByText(mockFetch.payload.itemResponse.body.name.trim())).toBeVisible()
        expect(amount().getByText((mockFetch.payload.itemResponse.body.amount + ' €').trim())).toBeVisible()
        expect(description().getByText(mockFetch.payload.itemResponse.body.description.trim())).toBeVisible()
        expect(isValid().getByTestId(data.isValid ? 'valid' : 'notValid')).toBeVisible()
    })
});

it('should render default values when absent in response', async () => {
    // given
    mockFetch = new MockFetch({
        itemResponse: {
            status: 200,
            body: { id: '1'}
        },
        isValidResponse: {
            status: 200
        }
    })

    spyOnFetch.mockImplementation(mockFetch.execute)

    // when
    await act(() => render(<InfoModal itemId={mockFetch.payload.itemResponse.body.id} />))

    // then
    expect(title().getByText(mockFetch.payload.itemResponse.body.name || 'Info')).toBeVisible()
    expect(amount().getByText(`${mockFetch.payload.itemResponse.body.amount || ''} €`.trim())).toBeVisible()
    expect(description().getByText(mockFetch.payload.itemResponse.body.description || '')).toBeVisible()
    expect(isValid().getByTestId("notValid")).toBeVisible()
})

it(`should get /items with parameter`, async () => {
    // when
    await act(() => render(<InfoModal itemId={mockFetch.payload.itemResponse.body.id} />))

    // then
    expect(fetchSpy).toHaveBeenCalledTimes(2)

    expect(fetchSpy).toHaveBeenCalledWith(
        `${BE_API}/items/${mockFetch.payload.itemResponse.body.id}`,
        expect.objectContaining({"method": "GET"})
    )

    expect(fetchSpy).toHaveBeenCalledWith(
        `${BE_API}/items/isValid/${mockFetch.payload.itemResponse.body.id}`,
        expect.objectContaining({"method": "GET"})
    )
});

it('should render default values when error', async () => {
    // given
    mockFetch = new MockFetch({
        itemResponse: {
            status: 400,
            body: {}
        },
        isValidResponse: {
            status: 400
        }
    })

    spyOnFetch.mockImplementation(mockFetch.execute)

    // when
    await act(() => render(<InfoModal itemId={'1'} />))

    // then
    expect(title().getByText('Info')).toBeVisible()
    expect(amount().getByText('€')).toBeVisible()
    expect(description().getByText('')).toBeVisible()
})

it('should render received values when wrong id', async () => {
    // given
    mockFetch = new MockFetch({
        itemResponse: {
            status: 200,
            body: {
                id: '1',
                name: 'name',
                description: 'description',
                amount: 1,
            }
        }
    })

    spyOnFetch.mockImplementation(mockFetch.execute)

    // when
    await act(() => render(<InfoModal itemId={'2'} />))

    // then
    expect(title().getByText(mockFetch.payload.itemResponse.body.name.trim())).toBeVisible()
    expect(amount().getByText((mockFetch.payload.itemResponse.body.amount + ' €').trim())).toBeVisible()
    expect(description().getByText(mockFetch.payload.itemResponse.body.description.trim())).toBeVisible()
})