import { IAuthApi } from '@/types/IApi';
import { http } from '../http';

export const createAuthApi = (): IAuthApi => {
    return {
        login: (arg) => http.post('/auth/login', arg),
        me: () => http.get('/auth/me'),
    };
};
