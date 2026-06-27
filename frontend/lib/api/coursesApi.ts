import { ICoursesApi } from '@/types/IApi';
import { http } from '../http';

export const createCoursesApi = (): ICoursesApi => {
    return {
        createCourse: (body) => http.post('/courses', body),
        getCatalogue: () => http.get('/courses/catalogue'),
    };
};
