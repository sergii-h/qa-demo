import {useTranslation} from 'react-i18next';
import {StyleSheet} from 'react-native';
import {Chip} from 'react-native-paper';

import {TaskPriority, TaskStatus} from '@/data/models/task';
import {taskPriorityLabel, taskStatusLabel} from '@/i18n/taskLabels';
import {chipColors} from '@/theme/colors';
import {TestTags} from '@/testTags';

interface StatusChipProps {
  status: TaskStatus;
}

export function StatusChip({ status }: StatusChipProps) {
  const { t } = useTranslation();
  const color = {
    [TaskStatus.TODO]: chipColors.todoBlue,
    [TaskStatus.IN_PROGRESS]: chipColors.inProgressOrange,
    [TaskStatus.DONE]: chipColors.doneGreen,
  }[status];

  return (
    <Chip
      compact
      disabled
      testID={TestTags.statusTag(status)}
      style={[styles.chip, { backgroundColor: `${color}26` }]}
      textStyle={{ color }}
    >
      {taskStatusLabel(t, status)}
    </Chip>
  );
}

interface PriorityChipProps {
  priority: TaskPriority;
}

export function PriorityChip({ priority }: PriorityChipProps) {
  const { t } = useTranslation();
  const color = {
    [TaskPriority.LOW]: chipColors.lowGreen,
    [TaskPriority.MEDIUM]: chipColors.mediumOrange,
    [TaskPriority.HIGH]: chipColors.highRed,
  }[priority];

  return (
    <Chip
      compact
      disabled
      testID={TestTags.priorityTag(priority)}
      style={[styles.chip, { backgroundColor: `${color}26` }]}
      textStyle={{ color }}
    >
      {taskPriorityLabel(t, priority)}
    </Chip>
  );
}

const styles = StyleSheet.create({
  chip: {
    alignSelf: 'flex-start',
  },
});
