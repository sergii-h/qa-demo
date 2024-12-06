import React, {useEffect, useState} from 'react';
import {Dialog} from 'primereact/dialog';
import {ProgressSpinner} from 'primereact/progressspinner';
import IItem from '../../interfaces/IItem';
import {getItem, getIsValid} from '../../helpers';

interface IProps {
    onClose: () => void;
    itemId: string;
}

export const InfoModal = (props: IProps) => {
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [item, setItem] = useState<IItem | null>(null);
    const [valid, setValid] = useState<boolean>(false);

    useEffect(() => {
        getItem(props.itemId).then((item: IItem) => {
            setItem(item);
            setIsLoading(false);
        });
       getIsValid(props.itemId).then((data: any) => setValid(JSON.stringify(data) === "true"));
    }, [props.itemId]);

    const onHide = () => {
        props.onClose();
    }

    return (
        <Dialog id="info-modal" header={item?.name || "Info"} visible={true} onHide={() => onHide()} style={{ minWidth: 480 }}>
            {isLoading ? <ProgressSpinner /> : <>
                <label className="block">Amount:</label>
                <p id="amount">{item?.amount} €</p>
                <label className="block">Description:</label>
                <p id="description">{item?.description}</p>
                <label id="valid" className="block">Validated: {
                    valid
                     ? <i data-testid="valid" className="pi pi-check" style={{ color: 'green' }}/>
                     : <i data-testid="notValid" className="pi pi-times" style={{ color: 'red' }}/>
                     }
                </label>

            </>}
        </Dialog>
    )
}
