import { IApi } from '@/types/IApi';
import { createAuthApi } from './authApi';
import { createRegistrationApi } from './registrationApi';
import { createAdminApi } from './adminApi';
import { createCoursesApi } from './coursesApi';
import { createStaticDataApi } from './staticDataApi';

export const createApi = (): IApi => {
    const authApi = createAuthApi();
    const registrationApi = createRegistrationApi();
    const adminApi = createAdminApi();
    const coursesApi = createCoursesApi();
    const staticDataApi = createStaticDataApi();

    return {
        auth: authApi,
        registration: registrationApi,
        admin: adminApi,
        courses: coursesApi,
        staticData: staticDataApi,
    };
};
