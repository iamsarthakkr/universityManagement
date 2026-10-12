import { IApi } from '@/types/IApi';
import { createAuthApi } from './authApi';
import { createRegistrationApi } from './registrationApi';
import { createCoursesApi } from './coursesApi';
import { createStaticDataApi } from './staticDataApi';
import { createSemestersApi } from './semestersApi';
import { createInstructorsApi } from './instructorsApi';
import { createStudentsApi } from './studentsApi';
import { createOfferingsApi } from './offeringsApi';
import { createEnrollmentsApi } from './enrollmentsApi';

export const createApi = (): IApi => {
    return {
        auth: createAuthApi(),
        registration: createRegistrationApi(),
        courses: createCoursesApi(),
        semesters: createSemestersApi(),
        instructors: createInstructorsApi(),
        students: createStudentsApi(),
        offerings: createOfferingsApi(),
        enrollments: createEnrollmentsApi(),
        staticData: createStaticDataApi(),
    };
};
