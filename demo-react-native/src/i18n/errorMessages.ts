import {TFunction} from 'i18next';

import {ApiError} from '@/data/remote/apiClient';

export function mapTaskError(t: TFunction, error: unknown): string {
  if (error instanceof ApiError) {
    if (error.message && !error.message.startsWith('Request failed')) {
      return error.message;
    }
    switch (error.status) {
      case 400:
        return t('errorInvalidTaskData');
      case 404:
        return t('errorTaskNotFound');
      case 409:
        return t('errorTitleAlreadyExists');
      default:
        return t('errorRequestFailed', { code: error.status });
    }
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return t('errorSomethingWentWrong');
}

export function isDuplicateTitleError(error: unknown): boolean {
  return error instanceof ApiError && error.status === 409;
}
