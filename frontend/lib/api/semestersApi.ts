import { ISemestersApi } from '@/types/IApi';
import { http } from '../http';

export const createSemestersApi = (): ISemestersApi => {
    return {
        getSemesters: () => http.get('/semesters'),
        getSemester: (id) => http.get(`/semesters/${id}`),
        createSemester: (body) => http.post('/semesters', body),
        transitionSemester: ({ semesterId, status }) => http.patch(`/semesters/${semesterId}/status`, { status }),
    };
};
