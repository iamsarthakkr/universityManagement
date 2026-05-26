import { AuthUser } from './auth';
import { RemoteCall, RemoteCallNoArgs } from './common';
import {
    InstructorRegistrationRequest,
    InstructorRegistrationResponse,
    StudentRegistrationRequest,
    StudentRegistrationResponse,
} from './registration';

export type LoginRequest = {
    username: string;
    password: string;
};
export type LoginResponse = {
    accessToken: string;
    user: AuthUser;
};

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
    getPendingStudentRegistrations: RemoteCallNoArgs<StudentRegistrationResponse[]>;
    approveStudentRegistration: RemoteCall<number, StudentRegistrationResponse>;
}
