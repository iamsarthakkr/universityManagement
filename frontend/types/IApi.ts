import { AuthUser, LoginRequest, LoginResponse } from './auth';
import { RemoteCall, RemoteCallNoArgs } from './common';
import { CourseCatalogueGroup, CourseRequest, CourseResponse } from './course';
import { Department } from './department';
import { Enrollment, EnrollmentDetail, OfferingEnrollmentsQuery } from './enrollment';
import { Instructor } from './instructor';
import { CourseOffering, CreateCourseOfferingRequest } from './offering';
import {
    InstructorRegistrationRequest,
    InstructorRegistrationResponse,
    RegistrationStatus,
    StudentRegistrationRequest,
    StudentRegistrationResponse,
} from './registration';
import { CreateSemesterRequest, Semester, SemesterTransitionRequest } from './semester';

export interface IApi {
    auth: IAuthApi;
    registration: IRegistrationApi;
    admin: IAdminApi;
    courses: ICoursesApi;
    semesters: ISemestersApi;
    instructors: IInstructorsApi;
    offerings: IOfferingsApi;
    enrollments: IEnrollmentsApi;
    staticData: IStaticDataApi;
}

export interface IStaticDataApi {
    getDepartments: RemoteCallNoArgs<Department[]>;
}

export interface IAuthApi {
    login: RemoteCall<LoginRequest, LoginResponse>;
    me: RemoteCallNoArgs<AuthUser>;
}

export interface IRegistrationApi {
    createStudentRegistration: RemoteCall<StudentRegistrationRequest, StudentRegistrationResponse>;

    createInstructorRegistration: RemoteCall<InstructorRegistrationRequest, InstructorRegistrationResponse>;
}

export interface ICoursesApi {
    createCourse: RemoteCall<CourseRequest, CourseResponse>;
    getCatalogue: RemoteCallNoArgs<CourseCatalogueGroup[]>;
}

export interface IAdminApi {
    getStudentRegistrations: RemoteCall<RegistrationStatus, StudentRegistrationResponse[]>;
    approveStudentRegistration: RemoteCall<number, StudentRegistrationResponse>;
    rejectStudentRegistration: RemoteCall<number, StudentRegistrationResponse>;

    getInstructorRegistrations: RemoteCall<RegistrationStatus, InstructorRegistrationResponse[]>;
    approveInstructorRegistration: RemoteCall<number, InstructorRegistrationResponse>;
    rejectInstructorRegistration: RemoteCall<number, InstructorRegistrationResponse>;
}

export interface ISemestersApi {
    getSemesters: RemoteCallNoArgs<Semester[]>;
    getSemester: RemoteCall<number, Semester>;
    createSemester: RemoteCall<CreateSemesterRequest, Semester>;
    transitionSemester: RemoteCall<SemesterTransitionRequest, void>;
}

export interface IInstructorsApi {
    getInstructors: RemoteCallNoArgs<Instructor[]>;
}

export interface IOfferingsApi {
    createOffering: RemoteCall<CreateCourseOfferingRequest, CourseOffering>;
    getOfferings: RemoteCall<number, CourseOffering[]>;
    getOffering: RemoteCall<number, CourseOffering>;
    getEnrollments: RemoteCall<OfferingEnrollmentsQuery, EnrollmentDetail[]>;
    requestEnrollment: RemoteCall<number, Enrollment>;
}

export interface IEnrollmentsApi {
    getMyEnrollments: RemoteCallNoArgs<EnrollmentDetail[]>;
    approveEnrollment: RemoteCall<number, Enrollment>;
    rejectEnrollment: RemoteCall<number, Enrollment>;
    cancelEnrollment: RemoteCall<number, Enrollment>;
    dropEnrollment: RemoteCall<number, Enrollment>;
}
