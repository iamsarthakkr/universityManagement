import { IApi } from '@/types/IApi';
import { createAuthApi } from './authApi';
import { createRegistrationApi } from './registrationApi';
import { createAdminApi } from './adminApi';
import { createCoursesApi } from './coursesApi';

export const createApi = (): IApi => {
    const authApi = createAuthApi();
    const registrationApi = createRegistrationApi();
    const adminApi = createAdminApi();
    const coursesApi = createCoursesApi();

    return {
        auth: authApi,
        registration: registrationApi,
        admin: adminApi,
        courses: coursesApi,
    };
};
