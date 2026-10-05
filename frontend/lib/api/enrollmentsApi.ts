import { IEnrollmentsApi } from '@/types/IApi';
import { http } from '../http';

export const createEnrollmentsApi = (): IEnrollmentsApi => {
    return {
        getMyEnrollments: () => http.get('/enrollments/me'),
        approveEnrollment: (id) => http.post(`/enrollments/${id}/approve`),
        rejectEnrollment: (id) => http.post(`/enrollments/${id}/reject`),
        cancelEnrollment: (id) => http.post(`/enrollments/${id}/cancel`),
        dropEnrollment: (id) => http.post(`/enrollments/${id}/drop`),
    };
};
