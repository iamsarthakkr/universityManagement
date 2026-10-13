import { ICoursesApi } from '@/types/IApi';
import { http } from '../http';
import { withQuery } from '../queryString';

export const createCoursesApi = (): ICoursesApi => {
    return {
        getCourses: (filter) => http.get(withQuery('/courses', filter)),
        getCourse: (id) => http.get(`/courses/${id}`),
        createCourse: (body) => http.post('/courses', body),
    };
};
