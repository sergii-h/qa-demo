import {fireEvent, render, screen, waitFor} from '@testing-library/react';
import {CreateModal} from './createModal';
import * as React from 'react';
import {jest} from '@jest/globals'
import {BE_API} from "../../helpers";

const MockFetch = require('../../mocks/mockFetch').default;
const spyOnFetch = jest.spyOn(window, 'fetch')

it('should render with default values', () => {
    // when
    render(<CreateModal />)

    // then
    expect(screen.getByLabelText('Name')).toHaveProperty('value', '')
    expect(screen.getByLabelText('Amount')).toHaveProperty('value', "0")
    expect(screen.getByLabelText('Description')).toHaveProperty('value', '')
});

it('should render spinner when loading', () => {
    // when
    render(<CreateModal isLoading={true}/>)

    // then
    expect(document.querySelector('.p-progress-spinner')).toBeVisible()
    expect(screen.queryByLabelText('Name')).toBeNull()
});

it('should change values', () => {
    // given
    render(<CreateModal />)

    const nameElement = screen.getByLabelText('Name')
    const amountElement = screen.getByLabelText('Amount')
    const descriptionElement = screen.getByLabelText('Description')

    // when
    fireEvent.change(nameElement, {target: {value: "name"}})
    fireEvent.change(amountElement, {target: {value: 1}})
    fireEvent.change(descriptionElement, {target: {value: "description"}})

    // then
    expect(nameElement).toHaveProperty('value', "name")
    expect(amountElement).toHaveProperty('value', "1")
    expect(descriptionElement).toHaveProperty('value', "description")
});

[
    { name: "name",         amount: 1,   description: "description"  },
    { name: "first second", amount: -1,  description: "first second" },
    { name: " ",            amount: 0,   description: " "            },
    { name: "",             amount: 0.5, description: ""             },
].forEach((data) => {
    it(`should post /items with parameters { '${data.name}', ${data.amount}, '${data.description}' }`, async () => {
        // given
        const fetchSpy = spyOnFetch.mockImplementation(new MockFetch().execute)

        render(<CreateModal onClose={() => true} onSave={() => true}/>)

        const nameElement = screen.getByLabelText('Name')
        const amountElement = screen.getByLabelText('Amount')
        const descriptionElement = screen.getByLabelText('Description')

        fireEvent.change(nameElement, {target: {value: data.name}})
        fireEvent.change(amountElement, {target: {value: data.amount}})
        fireEvent.change(descriptionElement, {target: {value: data.description}})

        // when
        await waitFor(() => {
            screen
                .getAllByRole('button')
                .find(element => element.className === 'p-button p-component create-button')
                .click()
        })

        // then
        await waitFor(() =>
            expect(fetchSpy).toHaveBeenCalledWith(
                `${BE_API}/items`,
                expect.objectContaining({
                    "body": `{"name":"${data.name}","amount":${data.amount},"description":"${data.description}"}`,
                    "method": "POST"}
                )
            )
        )
    })
});

[
    { amount: "",   formattedAmount: "0" },
    { amount: "01", formattedAmount: "1" },
].forEach((data) => {
    it(`should post /items parameter '${data.amount}' with formatted parameter '${data.formattedAmount}`, async () => {
        // given
        const fetchSpy = spyOnFetch.mockImplementation(new MockFetch().execute)

        render(<CreateModal onClose={() => true} onSave={() => true}/>)

        const nameElement = screen.getByLabelText('Name')
        const amountElement = screen.getByLabelText('Amount')
        const descriptionElement = screen.getByLabelText('Description')

        fireEvent.change(nameElement, {target: {value: 'name'}})
        fireEvent.change(amountElement, {target: {value: data.amount}})
        fireEvent.change(descriptionElement, {target: {value: 'description'}})

        // when
        await waitFor(() => {
            screen
                .getAllByRole('button')
                .find(element => element.className === 'p-button p-component create-button')
                .click()
        })

        // then
        await waitFor(() =>
            expect(fetchSpy).toHaveBeenCalledWith(
                `${BE_API}/items`,
                expect.objectContaining({
                    "body": `{"name":"name","amount":${data.formattedAmount},"description":"description"}`,
                    "method": "POST"}
                )
            )
        )
    })
});

it("should not post /items when Close button is clicked", async () => {
    // given
    const fetchSpy = spyOnFetch.mockImplementation(new MockFetch().execute)

    render(<CreateModal onClose={() => true} onSave={() => true}/>)

    // when
    await waitFor(() =>
        screen
            .getAllByRole('button')
            .find(element => element.className.includes('close-button'))
            .click()
    )

    // then
    expect(fetchSpy).not.toHaveBeenCalled()
});
