import { IInstructorsApi } from '@/types/IApi';
import { http } from '../http';

export const createInstructorsApi = (): IInstructorsApi => {
    return {
        getInstructors: () => http.get('/instructor'),
    };
};
