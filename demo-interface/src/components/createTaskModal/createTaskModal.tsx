import {useRef, useState} from 'react';
import {Dialog} from 'primereact/dialog';
import {Button} from 'primereact/button';
import {ProgressSpinner} from 'primereact/progressspinner';
import {InputText} from 'primereact/inputtext';
import {InputTextarea} from 'primereact/inputtextarea';
import {Dropdown} from 'primereact/dropdown';
import {useTranslation} from 'react-i18next';
import {createTask} from '../../services';
import {TaskStatus, TaskPriority} from '../../interfaces/ITask';

const statusDropdownItemTemplate = (option: { label: string; value: TaskStatus }) => (
    <span data-testid={`status-dropdown-option-${option.value}`}>{option.label}</span>
);

const priorityDropdownItemTemplate = (option: { label: string; value: TaskPriority }) => (
    <span data-testid={`priority-dropdown-option-${option.value}`}>{option.label}</span>
);

interface IProps {
    onClose: () => void;
    onSave: () => void;
    isLoading?: boolean | false;
}

export const CreateTaskModal = (props: IProps) => {
    const { t } = useTranslation();
    const [title, setTitle] = useState<string>('');
    const [description, setDescription] = useState<string>('');
    const [status, setStatus] = useState<TaskStatus>(TaskStatus.TODO);
    const [priority, setPriority] = useState<TaskPriority>(TaskPriority.MEDIUM);
    const [titleError, setTitleError] = useState<string>('');
    const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
    const isSubmittingRef = useRef<boolean>(false);
    const isLoading = props.isLoading;

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
        setIsSubmitting(true);
        try {
            await createTask({ title, description, status, priority });
            props.onSave();
            props.onClose();
        } catch (error: any) {
            if (error.message && error.message.includes('already exists')) {
                setTitleError(t('common.titleAlreadyExists'));
            } else {
                setTitleError(t('createTaskModal.failedToCreate'));
            }
            isSubmittingRef.current = false;
            setIsSubmitting(false);
        }
    }

    const isSaveDisabled = !title.trim() || isSubmitting;

    const renderFooter = () => {
        return (
            <>
                <Button label={t('common.close')} icon="pi pi-times" onClick={onHide} className="p-button-text close-button" data-testid="close-button" />
                <Button 
                    label={t('createTaskModal.create')} 
                    icon="pi pi-check" 
                    onClick={onSave} 
                    autoFocus 
                    className="create-button"
                    data-testid="create-button"
                    disabled={isSaveDisabled}
                />
            </>
        );
    }

    return (
        <Dialog id="create-task-modal" header={<span data-testid="modal-title">{t('createTaskModal.title')}</span>} visible={true} onHide={onHide} footer={renderFooter()} style={{ width: '95vw', maxWidth: '560px' }}>
            {isLoading ? <ProgressSpinner data-testid="loading-spinner" /> : <>
                <div className="field">
                    <label htmlFor="title" className="block">{t('common.title')}</label>
                    <InputText
                        id="title"
                        data-testid="create-task-title-input"
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
                        id="status"
                        data-testid="status-dropdown"
                        value={status}
                        options={statusOptions}
                        itemTemplate={statusDropdownItemTemplate}
                        onChange={(e) => setStatus(e.value)}
                        placeholder={t('common.selectStatus')}
                        style={{ width: '100%' }}
                    />
                </div>
                <div className="field">
                    <label htmlFor="priority" className="block">{t('common.priority')}</label>
                    <Dropdown
                        id="priority"
                        data-testid="priority-dropdown"
                        value={priority}
                        options={priorityOptions}
                        itemTemplate={priorityDropdownItemTemplate}
                        onChange={(e) => setPriority(e.value)}
                        placeholder={t('common.selectPriority')}
                        style={{ width: '100%' }}
                    />
                </div>
            </>}
        </Dialog>
    )
}

export default CreateTaskModal;

