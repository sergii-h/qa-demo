import {useEffect} from 'react';
import {ScrollView, StyleSheet, View} from 'react-native';
import {ActivityIndicator, Appbar, Button, Text, TextInput,} from 'react-native-paper';
import {NativeStackScreenProps} from '@react-navigation/native-stack';
import {useTranslation} from 'react-i18next';

import {EnumPicker} from '@/components/EnumPicker';
import {TaskFormMode, useTaskForm} from '@/hooks/useTaskForm';
import {taskPriorityLabel, taskStatusLabel} from '@/i18n/taskLabels';
import {RootStackParamList} from '@/navigation/types';
import {TaskPriority, TaskStatus} from '@/data/models/task';

type Props = NativeStackScreenProps<RootStackParamList, 'CreateTask' | 'EditTask'>;

export function TaskFormScreen({ navigation, route }: Props) {
  const { t } = useTranslation();
  const isEdit = route.name === 'EditTask';
  const mode = isEdit ? TaskFormMode.EDIT : TaskFormMode.CREATE;
  const taskId = isEdit ? (route.params as RootStackParamList['EditTask']).taskId : undefined;

  const {
    isLoading,
    isSaving,
    title,
    description,
    status,
    priority,
    titleError,
    saveError,
    loadError,
    saveSucceeded,
    onTitleChange,
    onDescriptionChange,
    onStatusChange,
    onPriorityChange,
    save,
  } = useTaskForm(mode, taskId);

  useEffect(() => {
    if (saveSucceeded) {
      navigation.navigate('TaskList', {
        refreshTrigger: Date.now(),
      });
    }
  }, [navigation, saveSucceeded]);

  return (
    <View style={styles.container}>
      <Appbar.Header>
        <Appbar.BackAction
          onPress={() => navigation.goBack()}
          accessibilityLabel={t('back')}
          testID='close-button'
        />
        <Appbar.Content
          title={t(isEdit ? 'editTask' : 'newTask')}
          testID='modal-title'
        />
      </Appbar.Header>

      {isLoading ? (
        <View style={styles.centered}>
          <ActivityIndicator testID='loading-spinner' size="large" />
        </View>
      ) : loadError ? (
        <View style={styles.centered}>
          <Text testID='load-error'>{loadError}</Text>
        </View>
      ) : (
        <ScrollView
          contentContainerStyle={styles.form}
          keyboardShouldPersistTaps="handled"
        >
          <TextInput
            label={t('fieldTitle')}
            value={title}
            onChangeText={onTitleChange}
            mode="outlined"
            error={Boolean(titleError)}
            testID={
              isEdit
                ? 'edit-task-title-input'
                : 'create-task-title-input'
            }
          />
          {titleError ? (
            <Text
              variant="bodySmall"
              style={styles.error}
              testID='title-error'
            >
              {titleError}
            </Text>
          ) : null}

          <TextInput
            label={t('fieldDescription')}
            value={description}
            onChangeText={onDescriptionChange}
            mode="outlined"
            multiline
            numberOfLines={4}
            testID='task-description-input'
          />

          <EnumPicker
            label={t('fieldStatus')}
            value={status}
            options={Object.values(TaskStatus)}
            optionLabel={(value) => taskStatusLabel(t, value)}
            onSelected={onStatusChange}
            testID='status-dropdown'
            optionTestID={(status) => `status-dropdown-option-${status}`}
          />

          <EnumPicker
            label={t('fieldPriority')}
            value={priority}
            options={Object.values(TaskPriority)}
            optionLabel={(value) => taskPriorityLabel(t, value)}
            onSelected={onPriorityChange}
            testID='priority-dropdown'
            optionTestID={(priority) => `priority-dropdown-option-${priority}`}
          />

          {saveError ? (
            <Text
              variant="bodySmall"
              style={styles.error}
              testID='save-error'
            >
              {saveError}
            </Text>
          ) : null}

          <Button
            mode="contained"
            onPress={() => void save()}
            disabled={!title.trim() || isSaving}
            testID={isEdit ? 'save-button' : 'create-button'}
            style={styles.submitButton}
          >
            {isSaving ? (
              <ActivityIndicator
                testID='loading-spinner'
                size="small"
                color="#FFFFFF"
              />
            ) : (
              t(isEdit ? 'save' : 'create')
            )}
          </Button>
        </ScrollView>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FAFAFA',
  },
  centered: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 16,
  },
  form: {
    padding: 16,
    gap: 16,
  },
  error: {
    color: '#D32F2F',
    marginTop: -8,
  },
  submitButton: {
    marginTop: 8,
  },
});
