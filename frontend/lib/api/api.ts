import { IApi } from '@/types/IApi';
import { createAuthApi } from './authApi';
import { createRegistrationApi } from './registrationApi';
import { createAdminApi } from './adminApi';
import { createCoursesApi } from './coursesApi';
import { createStaticDataApi } from './staticDataApi';
import { createSemestersApi } from './semestersApi';
import { createInstructorsApi } from './instructorsApi';
import { createOfferingsApi } from './offeringsApi';
import { createEnrollmentsApi } from './enrollmentsApi';

export const createApi = (): IApi => {
    const authApi = createAuthApi();
    const registrationApi = createRegistrationApi();
    const adminApi = createAdminApi();
    const coursesApi = createCoursesApi();
    const semestersApi = createSemestersApi();
    const instructorsApi = createInstructorsApi();
    const offeringsApi = createOfferingsApi();
    const enrollmentsApi = createEnrollmentsApi();
    const staticDataApi = createStaticDataApi();

    return {
        auth: authApi,
        registration: registrationApi,
        admin: adminApi,
        courses: coursesApi,
        semesters: semestersApi,
        instructors: instructorsApi,
        offerings: offeringsApi,
        enrollments: enrollmentsApi,
        staticData: staticDataApi,
    };
};
