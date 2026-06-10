import { IAdminApi } from '@/types/IApi';
import { http } from '../http';

export const createAdminApi = (): IAdminApi => {
    return {
        getStudentRegistrations: (status) => http.get(`/admin/student-registrations/${status}`),
        approveStudentRegistration: (id) => http.post(`/admin/student-registrations/${id}/approve`),
        rejectStudentRegistration: (id) => http.post(`/admin/student-registrations/${id}/reject`),

        getInstructorRegistrations: (status) => http.get(`/admin/instructor-registrations/${status}`),
        approveInstructorRegistration: (id) => http.post(`/admin/instructor-registrations/${id}/approve`),
        rejectInstructorRegistration: (id) => http.post(`/admin/instructor-registrations/${id}/reject`),
    };
};
