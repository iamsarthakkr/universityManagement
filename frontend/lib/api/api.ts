import { IApi } from '@/types/IApi';
import { createAuthApi } from './authApi';
import { createRegistrationApi } from './registrationApi';

export const createApi = (): IApi => {
    const authApi = createAuthApi();
    const registrationApi = createRegistrationApi();

    return {
        auth: authApi,
        registration: registrationApi,
    };
};
