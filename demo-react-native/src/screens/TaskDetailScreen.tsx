import {StyleSheet, View} from 'react-native';
import {ActivityIndicator, Appbar, Text, useTheme,} from 'react-native-paper';
import {NativeStackScreenProps} from '@react-navigation/native-stack';
import {useTranslation} from 'react-i18next';

import {PriorityChip, StatusChip} from '@/components/TaskChips';
import {useTaskDetail} from '@/hooks/useTaskDetail';
import {RootStackParamList} from '@/navigation/types';
import {TestTags} from '@/testTags';

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
          testID={TestTags.CLOSE_BUTTON}
        />
        <Appbar.Content
          title={task?.title ?? t('taskInfo')}
          testID={TestTags.MODAL_TITLE}
        />
      </Appbar.Header>

      {isLoading ? (
        <View style={styles.centered}>
          <ActivityIndicator testID={TestTags.LOADING_SPINNER} size="large" />
        </View>
      ) : errorMessage ? (
        <View style={styles.centered}>
          <Text testID={TestTags.LOAD_ERROR}>{errorMessage}</Text>
        </View>
      ) : task ? (
        <View style={styles.content}>
          <DetailField
            label={t('detailDescription')}
            labelTestID={TestTags.DETAIL_DESCRIPTION_LABEL}
          >
            <Text variant="bodyLarge" testID={TestTags.DESCRIPTION}>
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
            <Text variant="bodyLarge" testID={TestTags.CREATED_DATE}>
              {formatDate(
                task.createdDate,
                i18n.language,
                t('dateTimeFormat'),
                t('notAvailable'),
              )}
            </Text>
          </DetailField>

          <DetailField label={t('detailLastUpdated')}>
            <Text variant="bodyLarge" testID={TestTags.UPDATED_DATE}>
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
            labelTestID={TestTags.DETAIL_VALIDATED_LABEL}
          >
            <Text
              variant="headlineSmall"
              style={{ color: isValid ? theme.colors.primary : theme.colors.error }}
              testID={isValid ? TestTags.VALID : TestTags.NOT_VALID}
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
