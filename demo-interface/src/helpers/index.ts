import IItem from "../interfaces/IItem";

const BE_API = process.env.REACT_APP_BE_API || "http://127.0.0.1:8080/v1";

const getItems = async (): Promise<IItem[]> => {
    const response = await fetch(BE_API + `/item`, { method: 'GET', headers: { 'Content-Type': 'application/json'} });

    return await response.json();
}

const getItem = async (itemId: string): Promise<IItem> => {
    const response = await fetch(BE_API + `/item/${itemId}`, { method: 'GET', headers: { 'Content-Type': 'application/json'} });

    return await response.json();
}

const createItem = async (item: IItem): Promise<IItem> => {
    const response = await fetch(BE_API + `/item`, { method: 'POST', body: JSON.stringify({ ...item }), headers: { 'Content-Type': 'application/json'} });

    return await response.json();
}

const updateItem = async (item: IItem): Promise<any> => {
    return await fetch(BE_API + `/item/${item.id}`, { method: 'PUT', body: JSON.stringify({ name: item.name, amount: item.amount, description: item.description }), headers: { 'Content-Type': 'application/json'} });
}

const deleteItem = async (itemId: string): Promise<any> => {
    return await fetch(BE_API + `/item/${itemId}`, { method: 'DELETE', headers: { 'Content-Type': 'application/json'} });
}

export  {
    getItems,
    getItem,
    createItem,
    updateItem,
    deleteItem,
    BE_API
}