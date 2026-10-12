import { IStudentsApi } from '@/types/IApi';
import { http } from '../http';
import { withQuery } from '../queryString';

export const createStudentsApi = (): IStudentsApi => {
    return {
        getStudents: (filter) => http.get(withQuery('/students', filter)),
    };
};
