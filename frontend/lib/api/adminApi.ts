import { IAdminApi } from '@/types/IApi';
import { http } from '../http';

export const createAdminApi = (): IAdminApi => {
    return {
        getPendingStudentRegistrations: () => http.get('/admin/student-registrations/pending'),
        approveStudentRegistration: (id) => http.post(`/admin/student-registrations/${id}/approve`),
    };
};
