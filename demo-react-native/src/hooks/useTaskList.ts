import {useCallback, useEffect, useRef, useState} from 'react';
import {useTranslation} from 'react-i18next';

import {Task} from '@/data/models/task';
import {mapTaskError} from '@/i18n/errorMessages';
import {taskRepository} from '@/repository/taskRepository';

export interface TaskListState {
  tasks: Task[];
  isLoading: boolean;
  isRefreshing: boolean;
  errorMessage: string | null;
  deletingTaskIds: Set<string>;
}

const initialState: TaskListState = {
  tasks: [],
  isLoading: true,
  isRefreshing: false,
  errorMessage: null,
  deletingTaskIds: new Set(),
};

export function useTaskList(refreshTrigger = 0) {
  const { t } = useTranslation();
  const [state, setState] = useState<TaskListState>(initialState);
  const isRefreshingRef = useRef(false);
  const inFlightDeletesRef = useRef(new Set<string>());

  const loadTasks = useCallback(async () => {
    setState((current) => ({ ...current, isLoading: true, errorMessage: null }));
    try {
      const tasks = await taskRepository.getTasks();
      setState((current) => ({ ...current, tasks, isLoading: false }));
    } catch (error) {
      setState((current) => ({
        ...current,
        tasks: [],
        isLoading: false,
        errorMessage: mapTaskError(t, error),
      }));
    }
  }, [t]);

  const refreshTasks = useCallback(async () => {
    if (isRefreshingRef.current) {
      return;
    }

    isRefreshingRef.current = true;
    setState((current) => ({ ...current, isRefreshing: true, errorMessage: null }));

    try {
      const tasks = await taskRepository.getTasks();
      setState((current) => ({ ...current, tasks, isRefreshing: false }));
    } catch (error) {
      setState((current) => ({
        ...current,
        isRefreshing: false,
        errorMessage: mapTaskError(t, error),
      }));
    } finally {
      isRefreshingRef.current = false;
    }
  }, [t]);

  const deleteTask = useCallback(
    async (task: Task) => {
      if (inFlightDeletesRef.current.has(task.id)) {
        return;
      }

      inFlightDeletesRef.current.add(task.id);
      setState((current) => ({
        ...current,
        deletingTaskIds: new Set(current.deletingTaskIds).add(task.id),
      }));

      try {
        await taskRepository.deleteTask(task.id);
        setState((current) => {
          const deletingTaskIds = new Set(current.deletingTaskIds);
          deletingTaskIds.delete(task.id);
          return {
            ...current,
            tasks: current.tasks.filter((item) => item.id !== task.id),
            deletingTaskIds,
            errorMessage: null,
          };
        });
      } catch (error) {
        setState((current) => {
          const deletingTaskIds = new Set(current.deletingTaskIds);
          deletingTaskIds.delete(task.id);
          return {
            ...current,
            deletingTaskIds,
            errorMessage: mapTaskError(t, error),
          };
        });
      } finally {
        inFlightDeletesRef.current.delete(task.id);
      }
    },
    [t],
  );

  const clearError = useCallback(() => {
    setState((current) => ({ ...current, errorMessage: null }));
  }, []);

  useEffect(() => {
    void loadTasks();
  }, [loadTasks]);

  useEffect(() => {
    if (refreshTrigger > 0) {
      void refreshTasks();
    }
  }, [refreshTrigger, refreshTasks]);

  return {
    ...state,
    refreshTasks,
    deleteTask,
    clearError,
  };
}
