import React, {useEffect, useRef, useState} from 'react';
import {Dialog} from 'primereact/dialog';
import {Button} from 'primereact/button';
import {ProgressSpinner} from 'primereact/progressspinner';
import {InputText} from 'primereact/inputtext';
import {InputTextarea} from 'primereact/inputtextarea';
import {Dropdown} from 'primereact/dropdown';
import {useTranslation} from 'react-i18next';
import ITask, {TaskStatus, TaskPriority} from '../../interfaces/ITask';
import {getTask, updateTask} from '../../services';

const statusDropdownItemTemplate = (option: { label: string; value: TaskStatus }) => (
    <span data-testid={`status-dropdown-option-${option.value}`}>{option.label}</span>
);

const priorityDropdownItemTemplate = (option: { label: string; value: TaskPriority }) => (
    <span data-testid={`priority-dropdown-option-${option.value}`}>{option.label}</span>
);

interface IProps {
    onClose: () => void;
    onSave: () => void;
    taskId: string;
}

export const EditTaskModal = (props: IProps) => {
    const { t } = useTranslation();
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [task, setTask] = useState<ITask | null>(null);
    const [title, setTitle] = useState<string>('');
    const [description, setDescription] = useState<string>('');
    const [status, setStatus] = useState<TaskStatus>(TaskStatus.TODO);
    const [priority, setPriority] = useState<TaskPriority>(TaskPriority.MEDIUM);
    const [titleError, setTitleError] = useState<string>('');
    const isSubmittingRef = useRef<boolean>(false);

    const statusOptions = [
        { label: t(`common.taskStatus.${TaskStatus.TODO}`), value: TaskStatus.TODO },
        { label: t(`common.taskStatus.${TaskStatus.IN_PROGRESS}`), value: TaskStatus.IN_PROGRESS },
        { label: t(`common.taskStatus.${TaskStatus.DONE}`), value: TaskStatus.DONE }
    ];

    const priorityOptions = [
        { label: t(`common.taskPriority.${TaskPriority.LOW}`), value: TaskPriority.LOW },
        { label: t(`common.taskPriority.${TaskPriority.MEDIUM}`), value: TaskPriority.MEDIUM },
        { label: t(`common.taskPriority.${TaskPriority.HIGH}`), value: TaskPriority.HIGH }
    ];

    useEffect(() => {
        setIsLoading(true);
        getTask(props.taskId)
            .then((task: ITask) => {
                setTask(task);
                setTitle(task.title);
                setDescription(task.description || '');
                setStatus(task.status);
                setPriority(task.priority);
            })
            .catch(() => {
                setTask(null);
                setTitle('');
                setDescription('');
                setStatus(TaskStatus.TODO);
                setPriority(TaskPriority.MEDIUM);
            })
            .finally(() => {
                setIsLoading(false);
            });
    }, [props.taskId]);

    const validateTitle = (): boolean => {
        if (title.trim().length > 100) {
            setTitleError(t('common.titleTooLong'));
            return false;
        }
        setTitleError('');
        return true;
    };

    const onHide = () => {
        props.onClose();
    }

    const onSave = async () => {
        if (isSubmittingRef.current) {
            return;
        }
        if (!validateTitle()) {
            return;
        }
        isSubmittingRef.current = true;
        setIsLoading(true);
        try {
            await updateTask({ id: props.taskId, title, description, status, priority });
            setIsLoading(false);
            props.onSave();
            props.onClose();
        } catch (error: any) {
            isSubmittingRef.current = false;
            setIsLoading(false);
            if (error.message && error.message.includes('already exists')) {
                setTitleError(t('common.titleAlreadyExists'));
            } else {
                setTitleError(t('editTaskModal.failedToUpdate'));
            }
        }
    }

    const isSaveDisabled = !title.trim();

    const renderFooter = () => {
        return (
            <>
                <Button label={t('common.close')} icon="pi pi-times" onClick={onHide} className="p-button-text close-button" data-testid="close-button" />
                <Button
                    label={t('editTaskModal.save')}
                    icon="pi pi-check"
                    onClick={onSave}
                    autoFocus
                    className="save-button"
                    data-testid="save-button"
                    disabled={isSaveDisabled}
                />
            </>
        );
    }

    return (
        <Dialog
            id="edit-task-modal"
            header={<span data-testid="modal-title">{task?.title ? t('editTaskModal.titleWithTask', { taskTitle: task.title }) : t('editTaskModal.title')}</span>}
            visible={true}
            onHide={onHide}
            footer={renderFooter()} style={{ width: '95vw', maxWidth: '560px' }}
        >
            {isLoading ? <ProgressSpinner data-testid="loading-spinner" /> : <>
                <div className="field">
                    <label htmlFor="title" className="block">{t('common.title')}</label>
                    <InputText
                        id="title"
                        data-testid="edit-task-title-input"
                        aria-describedby="title-help"
                        className={`block ${titleError ? 'p-invalid' : ''}`}
                        style={{ width: '100%' }}
                        value={title}
                        onChange={(e) => {
                            setTitle(e.target.value);
                            if (titleError) setTitleError('');
                        }}
                    />
                    {titleError && <small data-testid="title-error" className="p-error block">{titleError}</small>}
                </div>
                <div className="field">
                    <label htmlFor="description" className="block">{t('common.description')}</label>
                    <InputTextarea
                        id="description"
                        aria-describedby="description-help"
                        className="block"
                        style={{ width: '100%' }}
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        rows={5}
                        cols={10}
                    />
                </div>
                <div className="field">
                    <label htmlFor="status" className="block">{t('common.status')}</label>
                    <Dropdown
                        inputId="status"
                        ariaLabel={t('common.status')}
                        data-testid="status-dropdown"
                        value={status}
                        options={statusOptions}
                        itemTemplate={statusDropdownItemTemplate}
                        onChange={(e) => setStatus(e.value)}
                        placeholder={t('common.selectStatus')}
                        style={{ width: '100%' }}
                        pt={{
                            select: { 'aria-label': t('common.status') },
                        }}
                    />
                </div>
                <div className="field">
                    <label htmlFor="priority" className="block">{t('common.priority')}</label>
                    <Dropdown
                        inputId="priority"
                        ariaLabel={t('common.priority')}
                        data-testid="priority-dropdown"
                        value={priority}
                        options={priorityOptions}
                        itemTemplate={priorityDropdownItemTemplate}
                        onChange={(e) => setPriority(e.value)}
                        placeholder={t('common.selectPriority')}
                        style={{ width: '100%' }}
                        pt={{
                            select: { 'aria-label': t('common.priority') },
                        }}
                    />
                </div>
            </>}
        </Dialog>
    )
}

export default EditTaskModal;

