import React, {useEffect, useState} from 'react';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column';
import {Button} from 'primereact/button';
import {Tag} from 'primereact/tag';
import {useTranslation} from 'react-i18next';
import InfoTaskModal from '../infoTaskModal';
import EditTaskModal from '../editTaskModal';
import CreateTaskModal from '../createTaskModal';
import ITask, {TaskStatus, TaskPriority} from '../../interfaces/ITask';
import {deleteTask, getTasks} from '../../services';

export const TasksTable = () => {
    const { t, i18n } = useTranslation();
    const [tasks, setTasks] = useState<ITask[]>([]);
    const [activeTaskId, setActiveTaskId] = useState<string>('');
    const [isInfoModalOpen, setIsInfoModalOpen] = useState<boolean>(false);
    const [isEditModalOpen, setIsEditModalOpen] = useState<boolean>(false);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState<boolean>(false);

    const fetchTasks = async () => {
        try {
            const data = await getTasks();
            setTasks(Array.isArray(data) ? data : []);
        } catch (error) {
            console.error('Failed to fetch tasks', error);
            setTasks([]);
        }
    }

    useEffect(() => {
        fetchTasks();
    }, []);

    const deleteTaskById = async (taskId: string) => {
        try {
            const response = await deleteTask(taskId);
            if (!response?.ok) {
                console.error(`Failed to delete task ${taskId}`);
                return;
            }
            setTasks(prevTasks => prevTasks.filter(task => task.id !== taskId));
        } catch (error) {
            console.error(`Failed to delete task ${taskId}`, error);
        }
    }

    const titleBodyTemplate = (rowData: ITask) => (
        <span data-testid={`task-title-${rowData.id}`}>{rowData.title}</span>
    );

    const statusBodyTemplate = (rowData: ITask) => {
        const getSeverity = (status: TaskStatus) => {
            switch (status) {
                case TaskStatus.TODO: return 'info';
                case TaskStatus.IN_PROGRESS: return 'warning';
                case TaskStatus.DONE: return 'success';
            }
        };
        return <Tag data-testid={`status-tag-${rowData.status}`} value={t(`common.taskStatus.${rowData.status}`)} severity={getSeverity(rowData.status)} />;
    };

    const priorityBodyTemplate = (rowData: ITask) => {
        const getSeverity = (priority: TaskPriority) => {
            switch (priority) {
                case TaskPriority.LOW: return 'success';
                case TaskPriority.MEDIUM: return 'warning';
                case TaskPriority.HIGH: return 'danger';
            }
        };
        return <Tag data-testid={`priority-tag-${rowData.priority}`} value={t(`common.taskPriority.${rowData.priority}`)} severity={getSeverity(rowData.priority)} />;
    };

    const actionsBodyTemplate = (rowData: any) => {
        return (
            <>
                <Button
                    icon="pi pi-info-circle"
                    label={t('tasksTable.actions.info')}
                    data-testid={`info-button-${rowData.id}`}
                    onClick={() => {
                        setActiveTaskId(rowData.id);
                        setIsInfoModalOpen(true);
                    }}
                    className="p-button-outlined task-info-button task-action-button"
                    style={{ marginRight: 8 }}
                />
                <Button
                    icon="pi pi-pencil"
                    label={t('tasksTable.actions.edit')}
                    data-testid={`edit-button-${rowData.id}`}
                    onClick={() => {
                        setActiveTaskId(rowData.id);
                        setIsEditModalOpen(true);
                    }}
                    className="p-button-outlined edit-task-button task-action-button"
                    style={{ marginRight: 8 }}
                />
                <Button
                    icon="pi pi-trash"
                    label={t('tasksTable.actions.delete')}
                    data-testid={`delete-button-${rowData.id}`}
                    onClick={() => {
                        deleteTaskById(rowData.id);
                    }}
                    className="p-button-outlined p-button-danger delete-task-button task-action-button"
                />
            </>
        );
    }

    return (
        <div className="tasks-table-container">
            <h1 className="page-title" data-testid="page-title">{t('tasksTable.title')}</h1>
            <Button
                icon="pi pi-plus"
                label={t('tasksTable.createTask')}
                data-testid="add-task-button"
                onClick={() => setIsCreateModalOpen(true)}
                className="p-button-info add-task-button"
            />
            <div className="card">
                <DataTable key={i18n.resolvedLanguage} className="tasks-table" value={tasks} stripedRows responsiveLayout="scroll">
                    <Column field="title" header={t('tasksTable.columns.title')} body={titleBodyTemplate} style={{ width: '30%' }} />
                    <Column field="status" header={t('tasksTable.columns.status')} body={statusBodyTemplate} style={{ width: '15%' }} />
                    <Column field="priority" header={t('tasksTable.columns.priority')} body={priorityBodyTemplate} style={{ width: '15%' }} />
                    <Column field="actions" header={t('tasksTable.columns.actions')} body={actionsBodyTemplate} style={{ whiteSpace: 'nowrap' }} />
                </DataTable>
            </div>
            {isInfoModalOpen && <InfoTaskModal taskId={activeTaskId} onClose={() => setIsInfoModalOpen(false)} />}
            {isEditModalOpen && <EditTaskModal
                taskId={activeTaskId}
                onClose={() => setIsEditModalOpen(false)}
                onSave={async () => await fetchTasks()}
            />}
            {isCreateModalOpen && <CreateTaskModal
                onClose={() => setIsCreateModalOpen(false)}
                onSave={async () => await fetchTasks()}
                isLoading={false}
            />}
        </div>
    );
}

export default TasksTable;

