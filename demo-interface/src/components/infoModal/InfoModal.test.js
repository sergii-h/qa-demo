import {act, render, within} from '@testing-library/react';
import * as React from 'react';
import {jest} from '@jest/globals'
import {InfoModal} from "./infoModal";
import {BE_API} from "../../helpers";

const title = () => within(document.querySelector('.p-dialog-title'))
const amount = () => within(document.querySelector('#info-modal_content > p:nth-child(2)'))
const description = () => within(document.querySelector('#info-modal_content > p:last-child'))

const MockFetch = require('../../mocks/mockFetch').default;
const spyOnFetch = jest.spyOn(window, 'fetch')

let mockFetch
let fetchSpy

beforeEach(() => {
    mockFetch = new MockFetch()
    fetchSpy = spyOnFetch.mockImplementation(mockFetch.execute)
});

[
    { name: "name",         amount: 1,     description: "description"         },
    { name: "name",         amount: 0.5,   description: "description"         },
    { name: "name",         amount: "0,5", description: "description"         },
    { name: "another name", amount: -1,    description: "another description" },
    { name: " ",            amount: 0,     description: " "                   },
    { name: "name",         amount: "",    description: ""                    },
    { name: "name",         amount: "01",  description: ""                    },
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
            }
        })

        spyOnFetch.mockImplementation(mockFetch.execute)

        // when
        await act(() => render(<InfoModal itemId={mockFetch.payload.itemResponse.body.id} />))

        // then
        expect(title().getByText(mockFetch.payload.itemResponse.body.name.trim())).toBeVisible()
        expect(amount().getByText((mockFetch.payload.itemResponse.body.amount + ' €').trim())).toBeVisible()
        expect(description().getByText(mockFetch.payload.itemResponse.body.description.trim())).toBeVisible()
    })
});

[
    { id: '1',               amount: 1,    description: "description" },
    { id: '1', name: 'name',               description: "description" },
    { id: '1', name: 'name', amount: 1                                },
].forEach((body) => {
    it('should render default values when absent in response', async () => {
        // given
        mockFetch = new MockFetch({
            itemResponse: {
                status: 200,
                body: body
            }
        })

        spyOnFetch.mockImplementation(mockFetch.execute)

        // when
        await act(() => render(<InfoModal itemId={mockFetch.payload.itemResponse.body.id} />))

        // then
        expect(title().getByText(mockFetch.payload.itemResponse.body.name || 'Info')).toBeVisible()
        expect(amount().getByText(`${mockFetch.payload.itemResponse.body.amount || ''} €`.trim())).toBeVisible()
        expect(description().getByText(mockFetch.payload.itemResponse.body.description || '')).toBeVisible()
    })
});

it(`should get /item with parameter`, async () => {
    // when
    await act(() => render(<InfoModal itemId={mockFetch.payload.itemResponse.body.id} />))

    // then
    expect(fetchSpy).toHaveBeenCalledTimes(1)

    expect(fetchSpy).toHaveBeenCalledWith(
        `${BE_API}/item/${mockFetch.payload.itemResponse.body.id}`,
        expect.objectContaining({"method": "GET"})
    )
});

it('should render default values when error', async () => {
    // given
    mockFetch = new MockFetch({
        itemResponse: {
            status: 400,
            body: {}
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