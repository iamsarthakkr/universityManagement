import { IOfferingsApi } from '@/types/IApi';
import { http } from '../http';

export const createOfferingsApi = (): IOfferingsApi => {
    return {
        createOffering: (body) => http.post('/course-offerings', body),
        getOfferings: (semesterId) => http.get(`/course-offerings?semesterId=${semesterId}`),
        getOffering: (id) => http.get(`/course-offerings/${id}`),
        getEnrollments: ({ offeringId, status }) => {
            const query = status ? `?enrollmentStatus=${status}` : '';
            return http.get(`/course-offerings/${offeringId}/enrollments${query}`);
        },
        requestEnrollment: (offeringId) => http.post(`/course-offerings/${offeringId}/enrollments`),
    };
};
