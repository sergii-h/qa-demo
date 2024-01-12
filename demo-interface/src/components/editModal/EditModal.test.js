import {act, fireEvent, render, screen, waitFor} from '@testing-library/react';
import {EditModal} from './editModal';
import {jest} from '@jest/globals';

import {itemResponse, mockGetItem} from '../../mocks/mockFetch';
import * as React from "react";
import {BE_API} from "../../helpers";

let fetchSpy;

beforeEach(() => {
    itemResponse.body = {
        id: '1',
        name: 'name1',
        description: 'description1',
        amount: '1',
    }

    fetchSpy = jest.spyOn(window, 'fetch').mockImplementation(mockGetItem)
})

it('should render with mocked values', async () => {
    // when
    await act(() => render(<EditModal itemId={itemResponse.body.id} />))

    // then
    await waitFor(() => {
        expect(screen.getByLabelText('Name')).toHaveProperty('value', itemResponse.body.name)
        expect(screen.getByLabelText('Amount')).toHaveProperty('value', itemResponse.body.amount)
        expect(screen.getByLabelText('Description')).toHaveProperty('value', itemResponse.body.description)
    })

});

it('should get /item when rendered', () => {
    // when
    act(() => render(<EditModal itemId={itemResponse.body.id}/>))

    // then
    waitFor(() => {
        expect(fetchSpy).toHaveBeenCalledWith(
            `${BE_API}/item/1`,
            expect.objectContaining({
                "body": `{
                "name":"${itemResponse.body.name}",
                "amount":"${itemResponse.body.amount}",
                "description":"${itemResponse.body.description}"}`,
                "method": "GET"}
            )
        )
    })
});

[
    { name: "name2",        amount: 2,   description: "description2" },
    { name: "first second", amount: 0.5, description: "first second" },
    { name: "",             amount: -1,  description: ""             },
    { name: " ",            amount: 0,   description: " "            },
].forEach((data) => {
    it(`should change values with valid data { '${data.name}', ${data.amount}, '${data.description}' }`, () => {
        // given
        act(() => render(<EditModal itemId={itemResponse.body.id}/>))

        const nameElement = screen.getByLabelText('Name')
        const amountElement = screen.getByLabelText('Amount')
        const descriptionElement = screen.getByLabelText('Description')

        // when
        act(() => {
            fireEvent.change(nameElement, {target: {value: data.name}})
            fireEvent.change(amountElement, {target: {value: data.amount}})
            fireEvent.change(descriptionElement, {target: {value: data.description}})
        });

        // then
        waitFor(() => {
            expect(nameElement).toHaveProperty('value', data.name)
            expect(amountElement).toHaveProperty('value', `${data.amount}`)
            expect(descriptionElement).toHaveProperty('value', data.description)
        })
    })
});

[
    { name: "name2",        amount: 2,   description: "description2" },
    { name: "first second", amount: 0.5, description: "first second" },
    { name: "",             amount: -1,  description: ""             },
    { name: " ",            amount: 0,   description: " "            },
].forEach((data) => {
    it(`should put /item with valid payload { '${data.name}', ${data.amount}, '${data.description}' }`, () => {
        // given
        itemResponse.body = {
            id: '1',
            name: 'name1',
            description: 'description1',
            amount: '1',
        }

        act(() => render(<EditModal itemId={itemResponse.body.id} onClose={() => true} onSave={() => true}/>))

        const nameElement = screen.getByLabelText('Name')
        const amountElement = screen.getByLabelText('Amount')
        const descriptionElement = screen.getByLabelText('Description')

        act(() => {
            fireEvent.change(nameElement, {target: {value: data.name}})
            fireEvent.change(amountElement, {target: {value: data.amount}})
            fireEvent.change(descriptionElement, {target: {value: data.description}})
        });

        // when
        act(() => {
            screen
                .getAllByRole('button')
                .find(element => element.className.includes('save-button'))
                .click()
        })

        // then
        waitFor(() => {
            expect(fetchSpy).toHaveBeenCalledTimes(2)
            expect(fetchSpy).toHaveBeenLastCalledWith(
                `${BE_API}/item/1`,
                expect.objectContaining({
                    "body": `{"name":"${data.name}","amount":"${data.amount}","description":"${data.description}"}`,
                    "method": "PUT"}
                )
            )
        })
    })
});

it("should not put /item when Close button is clicked", () => {
    // given
    itemResponse.body = {
        id: '1',
        name: 'name1',
        description: 'description1',
        amount: '1',
    }

    act(() => render(<EditModal itemId={itemResponse.body.id} onClose={() => true}/>))

    // when
    waitFor(() => {
        screen
            .getAllByRole('button')
            .find(element => element.className.includes('close-button'))
            .click()
    })

    // then
    expect(fetchSpy).not.toHaveBeenCalledWith(
        `${BE_API}/item/1`,
        expect.objectContaining({"method": "PUT"})
    )
});
