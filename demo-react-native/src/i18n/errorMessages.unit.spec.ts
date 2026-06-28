import {TFunction} from 'i18next';

import {ApiError} from '@/data/remote/apiClient';
import {isDuplicateTitleError, mapTaskError} from './errorMessages';

const t = ((key: string, options?: { code?: number }) => {
  if (key === 'errorRequestFailed' && options?.code) {
    return `Request failed (${options.code})`;
  }
  return key;
}) as TFunction;

describe('errorMessages', () => {
  describe('mapTaskError', () => {
    it('should return api message when present', () => {
      const error = new ApiError(400, 'Custom message');

      expect(mapTaskError(t, error)).toBe('Custom message');
    });

    it('should map 400 status to invalid task data', () => {
      expect(mapTaskError(t, new ApiError(400, 'Request failed (400)'))).toBe(
        'errorInvalidTaskData',
      );
    });

    it('should map 404 status to task not found', () => {
      expect(mapTaskError(t, new ApiError(404, 'Request failed (404)'))).toBe(
        'errorTaskNotFound',
      );
    });

    it('should map 409 status to duplicate title', () => {
      expect(mapTaskError(t, new ApiError(409, 'Request failed (409)'))).toBe(
        'errorTitleAlreadyExists',
      );
    });

    it('should map unknown api status to request failed', () => {
      expect(mapTaskError(t, new ApiError(503, 'Request failed (503)'))).toBe(
        'Request failed (503)',
      );
    });

    it('should return error message for generic Error', () => {
      expect(mapTaskError(t, new Error('Network down'))).toBe('Network down');
    });

    it('should return generic message for unknown errors', () => {
      expect(mapTaskError(t, 'boom')).toBe('errorSomethingWentWrong');
    });
  });

  describe('isDuplicateTitleError', () => {
    it('should return true for 409 ApiError', () => {
      expect(isDuplicateTitleError(new ApiError(409, 'duplicate'))).toBe(true);
    });

    it('should return false for other errors', () => {
      expect(isDuplicateTitleError(new ApiError(400, 'bad'))).toBe(false);
      expect(isDuplicateTitleError(new Error('x'))).toBe(false);
    });
  });
});
