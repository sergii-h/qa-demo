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
import {TestTags} from '@/testTags';

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
          testID={TestTags.CLOSE_BUTTON}
        />
        <Appbar.Content
          title={t(isEdit ? 'editTask' : 'newTask')}
          testID={TestTags.MODAL_TITLE}
        />
      </Appbar.Header>

      {isLoading ? (
        <View style={styles.centered}>
          <ActivityIndicator testID={TestTags.LOADING_SPINNER} size="large" />
        </View>
      ) : loadError ? (
        <View style={styles.centered}>
          <Text testID={TestTags.LOAD_ERROR}>{loadError}</Text>
        </View>
      ) : (
        <ScrollView contentContainerStyle={styles.form}>
          <TextInput
            label={t('fieldTitle')}
            value={title}
            onChangeText={onTitleChange}
            mode="outlined"
            error={Boolean(titleError)}
            testID={
              isEdit
                ? TestTags.EDIT_TASK_TITLE_INPUT
                : TestTags.CREATE_TASK_TITLE_INPUT
            }
          />
          {titleError ? (
            <Text
              variant="bodySmall"
              style={styles.error}
              testID={TestTags.TITLE_ERROR}
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
            testID={TestTags.TASK_DESCRIPTION_INPUT}
          />

          <EnumPicker
            label={t('fieldStatus')}
            value={status}
            options={Object.values(TaskStatus)}
            optionLabel={(value) => taskStatusLabel(t, value)}
            onSelected={onStatusChange}
            testID={TestTags.STATUS_DROPDOWN}
            optionTestID={TestTags.statusDropdownOption}
          />

          <EnumPicker
            label={t('fieldPriority')}
            value={priority}
            options={Object.values(TaskPriority)}
            optionLabel={(value) => taskPriorityLabel(t, value)}
            onSelected={onPriorityChange}
            testID={TestTags.PRIORITY_DROPDOWN}
            optionTestID={TestTags.priorityDropdownOption}
          />

          {saveError ? (
            <Text
              variant="bodySmall"
              style={styles.error}
              testID={TestTags.SAVE_ERROR}
            >
              {saveError}
            </Text>
          ) : null}

          <Button
            mode="contained"
            onPress={() => void save()}
            disabled={!title.trim() || isSaving}
            testID={isEdit ? TestTags.SAVE_BUTTON : TestTags.CREATE_BUTTON}
            style={styles.submitButton}
          >
            {isSaving ? (
              <ActivityIndicator
                testID={TestTags.LOADING_SPINNER}
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
