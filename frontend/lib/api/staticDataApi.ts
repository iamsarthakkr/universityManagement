import { IStaticDataApi } from '@/types/IApi';
import { http } from '../http';

export const createStaticDataApi = (): IStaticDataApi => {
    return {
        getDepartments: () => http.get('/departments'),
    };
};
