import { AuthUser, LoginRequest, LoginResponse } from './auth';
import { RemoteCall, RemoteCallNoArgs } from './common';
import { CourseRequest, CourseResponse } from './course';
import {
    InstructorRegistrationRequest,
    InstructorRegistrationResponse,
    RegistrationStatus,
    StudentRegistrationRequest,
    StudentRegistrationResponse,
} from './registration';

export interface IApi {
    auth: IAuthApi;
    registration: IRegistrationApi;
    admin: IAdminApi;
    courses: ICoursesApi;
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
}

export interface IAdminApi {
    getStudentRegistrations: RemoteCall<RegistrationStatus, StudentRegistrationResponse[]>;
    approveStudentRegistration: RemoteCall<number, StudentRegistrationResponse>;
    rejectStudentRegistration: RemoteCall<number, StudentRegistrationResponse>;

    getInstructorRegistrations: RemoteCall<RegistrationStatus, InstructorRegistrationResponse[]>;
    approveInstructorRegistration: RemoteCall<number, InstructorRegistrationResponse>;
    rejectInstructorRegistration: RemoteCall<number, InstructorRegistrationResponse>;
}
