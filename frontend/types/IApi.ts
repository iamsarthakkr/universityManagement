import { AuthUser, LoginRequest, LoginResponse } from './auth';
import { RemoteCall, RemoteCallNoArgs } from './common';
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
}

export interface IAuthApi {
    login: RemoteCall<LoginRequest, LoginResponse>;
    me: RemoteCallNoArgs<AuthUser>;
}

export interface IRegistrationApi {
    createStudentRegistration: RemoteCall<StudentRegistrationRequest, StudentRegistrationResponse>;

    createInstructorRegistration: RemoteCall<InstructorRegistrationRequest, InstructorRegistrationResponse>;
}

export interface IAdminApi {
    getStudentRegistrations: RemoteCall<RegistrationStatus, StudentRegistrationResponse[]>;
    approveStudentRegistration: RemoteCall<number, StudentRegistrationResponse>;
    rejectStudentRegistration: RemoteCall<number, StudentRegistrationResponse>;

    getInstructorRegistrations: RemoteCall<RegistrationStatus, InstructorRegistrationResponse[]>;
    approveInstructorRegistration: RemoteCall<number, InstructorRegistrationResponse>;
    rejectInstructorRegistration: RemoteCall<number, InstructorRegistrationResponse>;
}
