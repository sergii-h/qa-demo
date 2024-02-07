import {act, fireEvent, render, screen} from '@testing-library/react';
import {EditModal} from './editModal';
import {jest} from '@jest/globals';

import {itemResponse, mockFetch} from '../../mocks/mockFetch';
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

    fetchSpy = jest.spyOn(window, 'fetch').mockImplementation(mockFetch)
});

it('should render with mocked values', async () => {
    // when
    await act(() => render(<EditModal itemId={itemResponse.body.id} />))

    // then
    expect(fetchSpy).toHaveBeenCalledTimes(1)

    expect(screen.getByText('Edit ' + itemResponse.body.name)).toBeVisible()
    expect(screen.getByLabelText('Name')).toHaveProperty('value', itemResponse.body.name)
    expect(screen.getByLabelText('Amount')).toHaveProperty('value', itemResponse.body.amount)
    expect(screen.getByLabelText('Description')).toHaveProperty('value', itemResponse.body.description)
});

[
    { name: "name2",        amount: 2,   description: "description2" },
    { name: "first second", amount: 0.5, description: "first second" },
    { name: "",             amount: -1,  description: ""             },
    { name: " ",            amount: 0,   description: " "            },
].forEach((data) => {
    it(`should change values with valid data { '${data.name}', ${data.amount}, '${data.description}' }`, async () => {
        // given
        await act(() => render(<EditModal itemId={itemResponse.body.id}/>))

        const nameElement = screen.getByLabelText('Name')
        const amountElement = screen.getByLabelText('Amount')
        const descriptionElement = screen.getByLabelText('Description')

        // when
        await act(() => {
            fireEvent.change(nameElement, {target: {value: data.name}})
            fireEvent.change(amountElement, {target: {value: data.amount}})
            fireEvent.change(descriptionElement, {target: {value: data.description}})
        });

        // then
        expect(nameElement).toHaveProperty('value', data.name)
        expect(amountElement).toHaveProperty('value', `${data.amount}`)
        expect(descriptionElement).toHaveProperty('value', data.description)
    })
});

[
    { name: "name2",        amount: 2,   description: "description2" },
    { name: "first second", amount: 0.5, description: "first second" },
    { name: "",             amount: -1,  description: ""             },
    { name: " ",            amount: 0,   description: " "            },
].forEach((data) => {
    it(`should put /item with valid payload { '${data.name}', ${data.amount}, '${data.description}' }`, async () => {
        // given
        itemResponse.body = {
            id: '1',
            name: 'name1',
            description: 'description1',
            amount: '1',
        }

        await act(() => render(<EditModal itemId={itemResponse.body.id} onClose={() => true} onSave={() => true}/>))

        const nameElement = screen.getByLabelText('Name')
        const amountElement = screen.getByLabelText('Amount')
        const descriptionElement = screen.getByLabelText('Description')

        await act(() => {
            fireEvent.change(nameElement, {target: {value: data.name}})
            fireEvent.change(amountElement, {target: {value: data.amount}})
            fireEvent.change(descriptionElement, {target: {value: data.description}})
        });

        // when
        await act(() => {
            screen
                .getAllByRole('button')
                .find(element => element.className.includes('save-button'))
                .click()
        })

        // then
        expect(fetchSpy).toHaveBeenCalledTimes(2)
        expect(fetchSpy).toHaveBeenLastCalledWith(
            `${BE_API}/item/1`,
            expect.objectContaining({
                "body": `{"name":"${data.name}","amount":${data.amount},"description":"${data.description}"}`,
                "method": "PUT"}
            )
        )
    })
});

it("should not put /item when Close button is clicked", async () => {
    // given
    await act(() => render(<EditModal itemId={itemResponse.body.id} onClose={() => true}/>))

    // when
    await act(() => {
        screen
            .getAllByRole('button')
            .find(element => element.className.includes('close-button'))
            .click()
    })

    // then
    expect(fetchSpy).not.toHaveBeenCalledWith(
        `${BE_API}/item/${itemResponse.body.id}`,
        expect.objectContaining({"method": "PUT"})
    )
});
