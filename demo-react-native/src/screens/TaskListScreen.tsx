import {FlatList, RefreshControl, StyleSheet, View,} from 'react-native';
import {ActivityIndicator, Appbar, Card, FAB, IconButton, Snackbar, Text,} from 'react-native-paper';
import {NativeStackScreenProps} from '@react-navigation/native-stack';
import {useTranslation} from 'react-i18next';

import {Task} from '@/data/models/task';
import {LanguageSwitcher} from '@/components/LanguageSwitcher';
import {PriorityChip, StatusChip} from '@/components/TaskChips';
import {useTaskList} from '@/hooks/useTaskList';
import {RootStackParamList} from '@/navigation/types';
import {TestTags} from '@/testTags';

type Props = NativeStackScreenProps<RootStackParamList, 'TaskList'>;

export function TaskListScreen({ navigation, route }: Props) {
  const { t } = useTranslation();
  const refreshTrigger = route.params?.refreshTrigger ?? 0;
  const {
    tasks,
    isLoading,
    isRefreshing,
    errorMessage,
    deletingTaskIds,
    refreshTasks,
    deleteTask,
    clearError,
  } = useTaskList(refreshTrigger);

  return (
    <View style={styles.container}>
      <Appbar.Header>
        <Appbar.Content
          title={t('tasksTitle')}
          titleStyle={styles.title}
          testID={TestTags.PAGE_TITLE}
        />
        <LanguageSwitcher />
      </Appbar.Header>

      {isRefreshing && <View testID={TestTags.REFRESHING} style={styles.hidden} />}

      {isLoading ? (
        <View style={styles.centered}>
          <ActivityIndicator testID={TestTags.LOADING_SPINNER} size="large" />
        </View>
      ) : (
        <FlatList
          testID={TestTags.TASK_LIST}
          data={tasks}
          keyExtractor={(item) => item.id}
          contentContainerStyle={
            tasks.length === 0 ? styles.emptyContainer : styles.listContent
          }
          refreshControl={
            <RefreshControl refreshing={isRefreshing} onRefresh={refreshTasks} />
          }
          ListEmptyComponent={
            <View style={styles.centered} testID={TestTags.EMPTY_TASKS}>
              <Text variant="bodyLarge">{t('emptyTasks')}</Text>
            </View>
          }
          renderItem={({ item }) => (
            <TaskRow
              task={item}
              isDeleting={deletingTaskIds.has(item.id)}
              onInfo={() => navigation.navigate('TaskDetail', { taskId: item.id })}
              onEdit={() => navigation.navigate('EditTask', { taskId: item.id })}
              onDelete={() => void deleteTask(item)}
            />
          )}
        />
      )}

      <FAB
        icon="plus"
        style={styles.fab}
        onPress={() => navigation.navigate('CreateTask')}
        accessibilityLabel={t('createTask')}
        testID={TestTags.ADD_TASK_BUTTON}
      />

      <Snackbar
        visible={Boolean(errorMessage)}
        onDismiss={clearError}
        duration={4000}
        testID={TestTags.ERROR_SNACKBAR}
      >
        {errorMessage}
      </Snackbar>
    </View>
  );
}

interface TaskRowProps {
  task: Task;
  isDeleting: boolean;
  onInfo: () => void;
  onEdit: () => void;
  onDelete: () => void;
}

function TaskRow({ task, isDeleting, onInfo, onEdit, onDelete }: TaskRowProps) {
  const { t } = useTranslation();

  return (
    <Card style={styles.card}>
      <Card.Content>
        <Text
          variant="titleMedium"
          style={styles.taskTitle}
          testID={TestTags.taskTitle(task.id)}
        >
          {task.title}
        </Text>
        <View style={styles.chipRow}>
          <StatusChip status={task.status} />
          <PriorityChip priority={task.priority} />
        </View>
        <View style={styles.actions}>
          <IconButton
            icon="information"
            onPress={onInfo}
            accessibilityLabel={t('actionInfo')}
            testID={TestTags.infoButton(task.id)}
          />
          <IconButton
            icon="pencil"
            onPress={onEdit}
            accessibilityLabel={t('actionEdit')}
            testID={TestTags.editButton(task.id)}
          />
          <IconButton
            icon="delete"
            onPress={onDelete}
            disabled={isDeleting}
            accessibilityLabel={t('actionDelete')}
            testID={TestTags.deleteButton(task.id)}
          />
        </View>
      </Card.Content>
    </Card>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FAFAFA',
  },
  title: {
    fontWeight: '600',
  },
  listContent: {
    padding: 16,
    gap: 12,
  },
  emptyContainer: {
    flexGrow: 1,
    justifyContent: 'center',
    padding: 16,
  },
  centered: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 48,
  },
  card: {
    marginBottom: 12,
  },
  taskTitle: {
    fontWeight: '600',
    marginBottom: 8,
  },
  chipRow: {
    flexDirection: 'row',
    gap: 8,
    marginBottom: 8,
  },
  actions: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
  },
  fab: {
    position: 'absolute',
    right: 16,
    bottom: 16,
  },
  hidden: {
    width: 1,
    height: 1,
  },
});
