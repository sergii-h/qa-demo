import IItem from "../interfaces/IItem";

const beApi = process.env.REACT_APP_BE_API || "http://localhost:8080/v1";

const getItems = async (): Promise<IItem[]> => {
    const response = await fetch(beApi + `/item`, { method: 'GET', headers: { 'Content-Type': 'application/json'} });

    return await response.json();
}

const getItem = async (itemId: string): Promise<IItem> => {
    const response = await fetch(beApi + `/item/${itemId}`, { method: 'GET', headers: { 'Content-Type': 'application/json'} });

    return await response.json();
}

const createItem = async (item: IItem): Promise<IItem> => {
    const response = await fetch(beApi + `/item`, { method: 'POST', body: JSON.stringify({ ...item }), headers: { 'Content-Type': 'application/json'} });

    return await response.json();
}

const updateItem = async (item: IItem): Promise<any> => {
    return await fetch(beApi + `/item/${item.id}`, { method: 'PUT', body: JSON.stringify({ name: item.name, description: item.description }), headers: { 'Content-Type': 'application/json'} });
}

const deleteItem = async (itemId: string): Promise<any> => {
    return await fetch(beApi + `/item/${itemId}`, { method: 'DELETE', headers: { 'Content-Type': 'application/json'} });
}

export  {
    getItems,
    getItem,
    createItem,
    updateItem,
    deleteItem
}