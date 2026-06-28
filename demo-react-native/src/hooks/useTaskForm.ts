import {useCallback, useEffect, useState} from 'react';
import {useTranslation} from 'react-i18next';

import {TaskPriority, TaskRequest, TaskStatus} from '@/data/models/task';
import {isDuplicateTitleError, mapTaskError,} from '@/i18n/errorMessages';
import {taskRepository} from '@/repository/taskRepository';

export enum TaskFormMode {
  CREATE = 'CREATE',
  EDIT = 'EDIT',
}

export interface TaskFormState {
  isLoading: boolean;
  isSaving: boolean;
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
  titleError: string | null;
  saveError: string | null;
  loadError: string | null;
  saveSucceeded: boolean;
}

const defaultFormState: TaskFormState = {
  isLoading: false,
  isSaving: false,
  title: '',
  description: '',
  status: TaskStatus.TODO,
  priority: TaskPriority.MEDIUM,
  titleError: null,
  saveError: null,
  loadError: null,
  saveSucceeded: false,
};

export function useTaskForm(mode: TaskFormMode, taskId?: string) {
  const { t } = useTranslation();
  const [state, setState] = useState<TaskFormState>(defaultFormState);

  const loadTask = useCallback(
    async (id: string) => {
      setState((current) => ({ ...current, isLoading: true, loadError: null }));
      try {
        const task = await taskRepository.getTask(id);
        setState((current) => ({
          ...current,
          isLoading: false,
          title: task.title,
          description: task.description ?? '',
          status: task.status,
          priority: task.priority,
        }));
      } catch (error) {
        setState((current) => ({
          ...current,
          isLoading: false,
          loadError: mapTaskError(t, error),
        }));
      }
    },
    [t],
  );

  useEffect(() => {
    if (mode === TaskFormMode.EDIT && taskId) {
      void loadTask(taskId);
    }
  }, [loadTask, mode, taskId]);

  const onTitleChange = useCallback((value: string) => {
    setState((current) => ({
      ...current,
      title: value,
      titleError: null,
      saveError: null,
    }));
  }, []);

  const onDescriptionChange = useCallback((value: string) => {
    setState((current) => ({ ...current, description: value }));
  }, []);

  const onStatusChange = useCallback((value: TaskStatus) => {
    setState((current) => ({ ...current, status: value }));
  }, []);

  const onPriorityChange = useCallback((value: TaskPriority) => {
    setState((current) => ({ ...current, priority: value }));
  }, []);

  const save = useCallback(async () => {
    const trimmedTitle = state.title.trim();
    if (!trimmedTitle) {
      return;
    }
    if (trimmedTitle.length > 100) {
      setState((current) => ({
        ...current,
        titleError: t('errorTitleTooLong'),
        saveError: null,
      }));
      return;
    }

    const request: TaskRequest = {
      title: trimmedTitle,
      description: state.description.trim() || null,
      status: state.status,
      priority: state.priority,
    };

    setState((current) => ({
      ...current,
      isSaving: true,
      titleError: null,
      saveError: null,
    }));

    try {
      if (mode === TaskFormMode.CREATE) {
        await taskRepository.createTask(request);
      } else if (taskId) {
        await taskRepository.updateTask(taskId, request);
      }
      setState((current) => ({ ...current, isSaving: false, saveSucceeded: true }));
    } catch (error) {
      if (isDuplicateTitleError(error)) {
        setState((current) => ({
          ...current,
          isSaving: false,
          titleError: t('errorTitleAlreadyExists'),
          saveError: null,
        }));
      } else {
        setState((current) => ({
          ...current,
          isSaving: false,
          titleError: null,
          saveError: mapTaskError(t, error),
        }));
      }
    }
  }, [mode, state, t, taskId]);

  return {
    ...state,
    onTitleChange,
    onDescriptionChange,
    onStatusChange,
    onPriorityChange,
    save,
  };
}
