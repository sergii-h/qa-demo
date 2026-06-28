import {useCallback, useEffect, useState} from 'react';
import {useTranslation} from 'react-i18next';

import {Task} from '@/data/models/task';
import {mapTaskError} from '@/i18n/errorMessages';
import {taskRepository} from '@/repository/taskRepository';

export interface TaskDetailState {
  isLoading: boolean;
  task: Task | null;
  isValid: boolean;
  errorMessage: string | null;
}

const initialState: TaskDetailState = {
  isLoading: true,
  task: null,
  isValid: false,
  errorMessage: null,
};

export function useTaskDetail(taskId: string) {
  const { t } = useTranslation();
  const [state, setState] = useState<TaskDetailState>(initialState);

  const load = useCallback(async () => {
    setState((current) => ({ ...current, isLoading: true, errorMessage: null }));

    const [taskResult, validResult] = await Promise.allSettled([
      taskRepository.getTask(taskId),
      taskRepository.isValid(taskId),
    ]);

    const task = taskResult.status === 'fulfilled' ? taskResult.value : null;
    const isValid = validResult.status === 'fulfilled' ? validResult.value : false;
    const errorMessage =
      taskResult.status === 'rejected'
        ? mapTaskError(t, taskResult.reason)
        : null;

    setState({
      isLoading: false,
      task,
      isValid,
      errorMessage,
    });
  }, [t, taskId]);

  useEffect(() => {
    void load();
  }, [load]);

  return state;
}
