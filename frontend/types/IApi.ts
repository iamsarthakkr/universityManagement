import { LoginRequest, LoginResponse, Me } from './auth';
import { RemoteCall, RemoteCallNoArgs } from './common';
import { Course, CourseFilter, CourseRequest } from './course';
import { Department } from './department';
import { Enrollment, EnrollmentActionRequest, EnrollmentFilter, OfferingEnrollmentsQuery } from './enrollment';
import { Instructor, InstructorFilter } from './instructor';
import { CourseOffering, CourseOfferingFilter, CreateCourseOfferingRequest, MyCourseOfferingFilter } from './offering';
import {
    InstructorRegistrationRequest,
    InstructorRegistrationResponse,
    RegistrationAction,
    RegistrationFilter,
    StudentRegistrationRequest,
    StudentRegistrationResponse,
} from './registration';
import { CreateSemesterRequest, Semester, SemesterActionRequest } from './semester';
import { Student, StudentFilter } from './student';

export interface IApi {
    auth: IAuthApi;
    registration: IRegistrationApi;
    courses: ICoursesApi;
    semesters: ISemestersApi;
    instructors: IInstructorsApi;
    students: IStudentsApi;
    offerings: IOfferingsApi;
    enrollments: IEnrollmentsApi;
    staticData: IStaticDataApi;
}

export interface IStaticDataApi {
    getDepartments: RemoteCallNoArgs<Department[]>;
}

export interface IAuthApi {
    login: RemoteCall<LoginRequest, LoginResponse>;
    me: RemoteCallNoArgs<Me>;
}

type RegistrationActionCall<T> = RemoteCall<{ registrationId: number; action: RegistrationAction }, T>;

export interface IRegistrationApi {
    createStudentRegistration: RemoteCall<StudentRegistrationRequest, StudentRegistrationResponse>;
    getStudentRegistrations: RemoteCall<RegistrationFilter, StudentRegistrationResponse[]>;
    performStudentRegistrationAction: RegistrationActionCall<StudentRegistrationResponse>;

    createInstructorRegistration: RemoteCall<InstructorRegistrationRequest, InstructorRegistrationResponse>;
    getInstructorRegistrations: RemoteCall<RegistrationFilter, InstructorRegistrationResponse[]>;
    performInstructorRegistrationAction: RegistrationActionCall<InstructorRegistrationResponse>;
}

export interface ICoursesApi {
    getCourses: RemoteCall<CourseFilter, Course[]>;
    getCourse: RemoteCall<number, Course>;
    createCourse: RemoteCall<CourseRequest, Course>;
}

export interface ISemestersApi {
    getSemesters: RemoteCallNoArgs<Semester[]>;
    getSemester: RemoteCall<number, Semester>;
    createSemester: RemoteCall<CreateSemesterRequest, Semester>;
    performAction: RemoteCall<SemesterActionRequest, Semester>;
}

export interface IInstructorsApi {
    getInstructors: RemoteCall<InstructorFilter, Instructor[]>;
}

export interface IStudentsApi {
    getStudents: RemoteCall<StudentFilter, Student[]>;
}

export interface IOfferingsApi {
    createOffering: RemoteCall<CreateCourseOfferingRequest, CourseOffering>;
    getOfferings: RemoteCall<CourseOfferingFilter, CourseOffering[]>;
    getMyOfferings: RemoteCall<MyCourseOfferingFilter, CourseOffering[]>;
    getOffering: RemoteCall<number, CourseOffering>;
    getEnrollments: RemoteCall<OfferingEnrollmentsQuery, Enrollment[]>;
    requestEnrollment: RemoteCall<number, Enrollment>;
}

export interface IEnrollmentsApi {
    getMyEnrollments: RemoteCall<EnrollmentFilter, Enrollment[]>;
    performAction: RemoteCall<EnrollmentActionRequest, Enrollment>;
}
