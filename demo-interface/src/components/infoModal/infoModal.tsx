import React, {useEffect, useState} from 'react';
import {Dialog} from 'primereact/dialog';
import {ProgressSpinner} from 'primereact/progressspinner';
import IItem from '../../interfaces/IItem';
import {getItem} from '../../helpers';

interface IProps {
    onClose: () => void;
    itemId: string;
}

export const InfoModal = (props: IProps) => {
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [item, setItem] = useState<IItem | null>(null);

    useEffect(() => {
        getItem(props.itemId).then((item: IItem) => {
            setItem(item);
            setIsLoading(false);
        });
    }, [props.itemId]);

    const onHide = () => {
        props.onClose();
    }

    return (
        <Dialog id="info-modal" header={item?.name || "Info"} visible={true} onHide={() => onHide()} style={{ minWidth: 480 }}>
            {isLoading ? <ProgressSpinner /> : <>
                <label className="block">Amount:</label>
                <p>{item?.amount} €</p>
                <label className="block">Description:</label>
                <p>{item?.description}</p>
            </>}
        </Dialog>
    )
}
