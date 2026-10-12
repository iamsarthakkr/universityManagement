import { IRegistrationApi } from '@/types/IApi';
import { http } from '../http';
import { withQuery } from '../queryString';

export const createRegistrationApi = (): IRegistrationApi => {
    return {
        createStudentRegistration: (body) => http.post('/student-registrations', body),
        getStudentRegistrations: (filter) => http.get(withQuery('/student-registrations', filter)),
        performStudentRegistrationAction: ({ registrationId, action }) =>
            http.post(`/student-registrations/${registrationId}/${action.toLowerCase()}`),

        createInstructorRegistration: (body) => http.post('/instructor-registrations', body),
        getInstructorRegistrations: (filter) => http.get(withQuery('/instructor-registrations', filter)),
        performInstructorRegistrationAction: ({ registrationId, action }) =>
            http.post(`/instructor-registrations/${registrationId}/${action.toLowerCase()}`),
    };
};
