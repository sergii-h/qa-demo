export type RootStackParamList = {
  TaskList: { refreshTrigger?: number } | undefined;
  CreateTask: undefined;
  EditTask: { taskId: string };
  TaskDetail: { taskId: string };
};
