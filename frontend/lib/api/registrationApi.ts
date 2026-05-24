import { IRegistrationApi } from '@/types/IApi';
import { http } from '../http';

export const createRegistrationApi = (): IRegistrationApi => {
    return {
        createStudentRegistration: (arg) => {
            return http.post('/registration/student', arg);
        },
        createInstructorRegistration: (arg) => {
            return http.post('/registration/instructor', arg);
        },
    };
};
