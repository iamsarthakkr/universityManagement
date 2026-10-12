import { IInstructorsApi } from '@/types/IApi';
import { http } from '../http';
import { withQuery } from '../queryString';

export const createInstructorsApi = (): IInstructorsApi => {
    return {
        getInstructors: (filter) => http.get(withQuery('/instructors', filter)),
    };
};
