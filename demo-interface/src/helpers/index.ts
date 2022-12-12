import IItem from "../interfaces/IItem";

const getItems = async (): Promise<IItem[]> => {
    const response = await fetch(`http://localhost:8080/v1/item`, { method: 'GET', headers: { 'Content-Type': 'application/json'} });

    return await response.json();
}

const getItem = async (itemId: string): Promise<IItem> => {
    const response = await fetch(`http://localhost:8080/v1/item/${itemId}`, { method: 'GET', headers: { 'Content-Type': 'application/json'} });

    return await response.json();
}

const createItem = async (item: IItem): Promise<IItem> => {
    const response = await fetch(`http://localhost:8080/v1/item`, { method: 'POST', body: JSON.stringify({ ...item }), headers: { 'Content-Type': 'application/json'} });

    return await response.json();
}

const updateItem = async (item: IItem): Promise<any> => {
    return await fetch(`http://localhost:8080/v1/item/${item.id}`, { method: 'PUT', body: JSON.stringify({ name: item.name, description: item.description }), headers: { 'Content-Type': 'application/json'} });
}

const deleteItem = async (itemId: string): Promise<any> => {
    return await fetch(`http://localhost:8080/v1/item/${itemId}`, { method: 'DELETE', headers: { 'Content-Type': 'application/json'} });
}

export  {
    getItems,
    getItem,
    createItem,
    updateItem,
    deleteItem
}