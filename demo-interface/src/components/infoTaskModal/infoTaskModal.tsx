import React, {useEffect, useState} from 'react';
import {Dialog} from 'primereact/dialog';
import {Button} from 'primereact/button';
import {ProgressSpinner} from 'primereact/progressspinner';
import {Tag} from 'primereact/tag';
import {useTranslation} from 'react-i18next';
import ITask, {TaskStatus, TaskPriority} from '../../interfaces/ITask';
import {getTask, getIsValid} from '../../services';

interface IProps {
    onClose: () => void;
    taskId: string;
}

export const InfoTaskModal = (props: IProps) => {
    const { t } = useTranslation();
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [task, setTask] = useState<ITask | null>(null);
    const [valid, setValid] = useState<boolean>(false);

    useEffect(() => {
        getTask(props.taskId)
            .then((task: ITask) => {
                setTask(task);
            })
            .catch(() => {
                setTask(null);
            })
            .finally(() => {
                setIsLoading(false);
            });

        getIsValid(props.taskId)
            .then((data: boolean) => setValid(data))
            .catch(() => setValid(false));
    }, [props.taskId]);

    const onHide = () => {
        props.onClose();
    }

    const getSeverityForStatus = (status: TaskStatus) => {
        switch (status) {
            case TaskStatus.TODO: return 'info';
            case TaskStatus.IN_PROGRESS: return 'warning';
            case TaskStatus.DONE: return 'success';
        }
    };

    const getSeverityForPriority = (priority: TaskPriority) => {
        switch (priority) {
            case TaskPriority.LOW: return 'success';
            case TaskPriority.MEDIUM: return 'warning';
            case TaskPriority.HIGH: return 'danger';
        }
    };

    const renderFooter = () => (
        <Button label={t('common.close')} data-testid="close-button" onClick={onHide} className="p-button-outlined close-button" />
    );

    return (
        <Dialog id="info-task-modal" header={<span data-testid="modal-title">{task?.title || t('infoTaskModal.title')}</span>} visible={true} onHide={onHide} footer={renderFooter()} style={{ width: '95vw', maxWidth: '560px' }}>
            {isLoading ? <ProgressSpinner data-testid="loading-spinner" /> : <>
                <label className="block">{t('infoTaskModal.description')}</label>
                <p data-testid="description">{task?.description || t('infoTaskModal.noDescription')}</p>

                <label className="block">{t('infoTaskModal.status')}</label>
                <p data-testid="status">
                    {task?.status && <Tag data-testid={`status-tag-${task.status}`} value={t(`common.taskStatus.${task.status}`)} severity={getSeverityForStatus(task.status)} />}
                </p>

                <label className="block">{t('infoTaskModal.priority')}</label>
                <p data-testid="priority">
                    {task?.priority && <Tag data-testid={`priority-tag-${task.priority}`} value={t(`common.taskPriority.${task.priority}`)} severity={getSeverityForPriority(task.priority)} />}
                </p>

                <label className="block">{t('infoTaskModal.created')}</label>
                <p data-testid="created-date">{task?.createdDate ? new Date(task.createdDate).toLocaleString() : t('infoTaskModal.na')}</p>

                <label className="block">{t('infoTaskModal.lastUpdated')}</label>
                <p data-testid="updated-date">{task?.updatedDate ? new Date(task.updatedDate).toLocaleString() : t('infoTaskModal.na')}</p>
                
                <label id="valid" className="block">{t('infoTaskModal.validated')} {
                    valid
                     ? <i data-testid="valid" className="pi pi-check" style={{ color: 'green' }}/>
                     : <i data-testid="notValid" className="pi pi-times" style={{ color: 'red' }}/>
                     }
                </label>
            </>}
        </Dialog>
    )
}

export default InfoTaskModal;

