import { IApi } from '@/types/IApi';
import { createAuthApi } from './authApi';
import { createRegistrationApi } from './registrationApi';
import { createAdminApi } from './adminApi';

export const createApi = (): IApi => {
    const authApi = createAuthApi();
    const registrationApi = createRegistrationApi();
    const adminApi = createAdminApi();

    return {
        auth: authApi,
        registration: registrationApi,
        admin: adminApi,
    };
};
