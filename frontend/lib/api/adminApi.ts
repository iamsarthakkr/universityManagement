import { IAdminApi } from '@/types/IApi';
import { http } from '../http';

export const createAdminApi = (): IAdminApi => {
    return {
        getStudentRegistrations: (status) => http.get(`/admin/student-registrations/${status}`),
        approveStudentRegistration: (id) => http.post(`/admin/student-registrations/${id}/approve`),
        rejectStudentRegistration: (id) => http.post(`/admin/student-registrations/${id}/reject`),
    };
};
