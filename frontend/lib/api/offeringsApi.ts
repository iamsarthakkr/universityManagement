import { IOfferingsApi } from '@/types/IApi';
import { http } from '../http';
import { withQuery } from '../queryString';

export const createOfferingsApi = (): IOfferingsApi => {
    return {
        createOffering: (body) => http.post('/course-offerings', body),
        getOfferings: (filter) => http.get(withQuery('/course-offerings', filter)),
        getMyOfferings: (filter) => http.get(withQuery('/me/course-offerings', filter)),
        getOffering: (id) => http.get(`/course-offerings/${id}`),
        getEnrollments: ({ offeringId, status }) =>
            http.get(withQuery(`/course-offerings/${offeringId}/enrollments`, { status })),
        requestEnrollment: (offeringId) => http.post(`/course-offerings/${offeringId}/enrollments`),
    };
};
