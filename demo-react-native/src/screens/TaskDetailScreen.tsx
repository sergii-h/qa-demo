import {StyleSheet, View} from 'react-native';
import {ActivityIndicator, Appbar, Text, useTheme,} from 'react-native-paper';
import {NativeStackScreenProps} from '@react-navigation/native-stack';
import {useTranslation} from 'react-i18next';

import {PriorityChip, StatusChip} from '@/components/TaskChips';
import {useTaskDetail} from '@/hooks/useTaskDetail';
import {RootStackParamList} from '@/navigation/types';

type Props = NativeStackScreenProps<RootStackParamList, 'TaskDetail'>;

function formatDate(
  value: string | null | undefined,
  locale: string,
  pattern: string,
  notAvailable: string,
): string {
  if (!value?.trim()) {
    return notAvailable;
  }
  try {
    const date = new Date(value);
    return new Intl.DateTimeFormat(locale, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    }).format(date);
  } catch {
    return value;
  }
}

export function TaskDetailScreen({ navigation, route }: Props) {
  const { t, i18n } = useTranslation();
  const theme = useTheme();
  const { taskId } = route.params;
  const { isLoading, task, isValid, errorMessage } = useTaskDetail(taskId);

  return (
    <View style={styles.container}>
      <Appbar.Header>
        <Appbar.BackAction
          onPress={() => navigation.goBack()}
          accessibilityLabel={t('back')}
          testID='close-button'
        />
        <Appbar.Content
          title={task?.title ?? t('taskInfo')}
          testID='modal-title'
        />
      </Appbar.Header>

      {isLoading ? (
        <View style={styles.centered}>
          <ActivityIndicator testID='loading-spinner' size="large" />
        </View>
      ) : errorMessage ? (
        <View style={styles.centered}>
          <Text testID='load-error'>{errorMessage}</Text>
        </View>
      ) : task ? (
        <View style={styles.content}>
          <DetailField
            label={t('detailDescription')}
            labelTestID={'detail-description-label'}
          >
            <Text variant="bodyLarge" testID='description'>
              {task.description?.trim() || t('noDescription')}
            </Text>
          </DetailField>

          <DetailField label={t('detailStatus')}>
            <StatusChip status={task.status} />
          </DetailField>

          <DetailField label={t('detailPriority')}>
            <PriorityChip priority={task.priority} />
          </DetailField>

          <DetailField label={t('detailCreated')}>
            <Text variant="bodyLarge" testID='created-date'>
              {formatDate(
                task.createdDate,
                i18n.language,
                t('dateTimeFormat'),
                t('notAvailable'),
              )}
            </Text>
          </DetailField>

          <DetailField label={t('detailLastUpdated')}>
            <Text variant="bodyLarge" testID='updated-date'>
              {formatDate(
                task.updatedDate,
                i18n.language,
                t('dateTimeFormat'),
                t('notAvailable'),
              )}
            </Text>
          </DetailField>

          <DetailField
            label={t('detailValidated')}
            labelTestID={'detail-validated-label'}
          >
            <Text
              variant="headlineSmall"
              style={{ color: isValid ? theme.colors.primary : theme.colors.error }}
              testID={isValid ? 'valid' : 'notValid'}
              accessibilityLabel={t(isValid ? 'valid' : 'notValid')}
            >
              {isValid ? '✓' : '✕'}
            </Text>
          </DetailField>
        </View>
      ) : null}
    </View>
  );
}

interface DetailFieldProps {
  label: string;
  labelTestID?: string;
  children: React.ReactNode;
}

function DetailField({ label, labelTestID, children }: DetailFieldProps) {
  return (
    <View style={styles.field}>
      <Text
        variant="labelLarge"
        style={styles.label}
        testID={labelTestID}
      >
        {label}
      </Text>
      {children}
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
  content: {
    padding: 16,
    gap: 12,
  },
  field: {
    gap: 4,
  },
  label: {
    fontWeight: '600',
  },
});
