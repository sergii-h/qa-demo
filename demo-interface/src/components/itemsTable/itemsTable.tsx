import React, {useEffect, useState} from 'react';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column';
import {Button} from 'primereact/button';
import InfoModal from '../infoModal';
import EditModal from '../editModal';
import CreateModal from '../createModal';
import IItem from '../../interfaces/IItem';
import {deleteItem, getItems} from '../../helpers';

export const ItemsTable = () => {
    const [items, setItems] = useState<IItem[]>([]);
    const [activeItemId, setActiveItemId] = useState<string>('');
    const [isInfoModalOpen, setIsInfoModalOpen] = useState<boolean>(false);
    const [isEditModalOpen, setIsEditModalOpen] = useState<boolean>(false);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState<boolean>(false);

    const fetchItems = async () => {
        await getItems().then(data => setItems(data));
    }

    useEffect(() => {
        fetchItems();
    }, []);

    const deleteItemById = async (itemId: string) => {
        await deleteItem(itemId);
        setItems(items.filter(item => item.id !== itemId));
    }

    const infoBodyTemplate = (rowData: any) => {
        return (
            <>
                <Button
                    icon="pi pi-info-circle"
                    label="Info"
                    onClick={() => {
                        setActiveItemId(rowData.id);
                        setIsInfoModalOpen(true);
                    }}
                    className="p-button-outlined item-inf0-button"
                    style={{ marginRight: 8 }}
                />
                <Button
                    icon="pi pi-pencil"
                    label="Edit"
                    onClick={() => {
                        setActiveItemId(rowData.id);
                        setIsEditModalOpen(true);
                    }}
                    className="p-button-outlined edit-item-button"
                    style={{ marginRight: 8 }}
                />
                <Button
                    icon="pi pi-trash"
                    label="Delete"
                    onClick={() => {
                        deleteItemById(rowData.id);
                    }}
                    className="p-button-outlined p-button-danger delete-item-button"
                />
            </>
        );
    }

    return (
        <div>
            <Button
                icon="pi pi-plus"
                label="Create item"
                onClick={() => setIsCreateModalOpen(true)}
                className="p-button-info add-item-button"
            />
            <div className="card">
                <DataTable className="items-table" value={items} stripedRows responsiveLayout="scroll">
                    <Column field="name" header="Name" style={{ width: '100%' }} />
                    <Column field="actions" header="Actions" body={infoBodyTemplate} style={{ whiteSpace: 'nowrap' }} />
                </DataTable>
            </div>
            {isInfoModalOpen && <InfoModal itemId={activeItemId} onClose={() => setIsInfoModalOpen(false)} />}
            {isEditModalOpen && <EditModal
                itemId={activeItemId}
                onClose={() => setIsEditModalOpen(false)}
                onSave={async () => await fetchItems()}
            />}
            {isCreateModalOpen && <CreateModal
                onClose={() => setIsCreateModalOpen(false)}
                onSave={async () => await fetchItems()}
                isLoading={false}
            />}
        </div>
    );
}
