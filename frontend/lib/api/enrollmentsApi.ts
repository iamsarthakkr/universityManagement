import { IEnrollmentsApi } from '@/types/IApi';
import { http } from '../http';
import { withQuery } from '../queryString';

export const createEnrollmentsApi = (): IEnrollmentsApi => {
    return {
        getMyEnrollments: (filter) => http.get(withQuery('/me/enrollments', filter)),
        performAction: ({ enrollmentId, action }) => http.post(`/enrollments/${enrollmentId}/${action.toLowerCase()}`),
    };
};
