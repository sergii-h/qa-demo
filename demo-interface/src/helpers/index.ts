import IItem from "../interfaces/IItem";

const BE_API = process.env.REACT_APP_BE_API || "http://localhost:8080/v1";

const getItems = async (): Promise<IItem[]> => {
    const response = await fetch(BE_API + `/items`, { method: 'GET', headers: { 'Content-Type': 'application/json'} });

    return await response.json();
}

const getItem = async (itemId: string): Promise<IItem> => {
    const response = await fetch(BE_API + `/items/${itemId}`, { method: 'GET', headers: { 'Content-Type': 'application/json'} });

    return await response.json();
}

const getIsValid = async (itemId: string): Promise<IItem> => {
    const response = await fetch(BE_API + `/items/isValid/${itemId}`, { method: 'GET', headers: { 'Content-Type': 'application/json'} });

    return await response.json();
}

const createItem = async (item: IItem): Promise<IItem> => {
    const response = await fetch(BE_API + `/items`, { method: 'POST', body: JSON.stringify({ ...item }), headers: { 'Content-Type': 'application/json'} });

    return await response.json();
}

const updateItem = async (item: IItem): Promise<any> => {
    return await fetch(BE_API + `/items/${item.id}`, { method: 'PUT', body: JSON.stringify({ name: item.name, amount: item.amount, description: item.description }), headers: { 'Content-Type': 'application/json'} });
}

const deleteItem = async (itemId: string): Promise<any> => {
    return await fetch(BE_API + `/items/${itemId}`, { method: 'DELETE', headers: { 'Content-Type': 'application/json'} });
}

export  {
    getItems,
    getItem,
    getIsValid,
    createItem,
    updateItem,
    deleteItem,
    BE_API
}