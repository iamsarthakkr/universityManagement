import { AuthUser } from './auth';
import { RemoteCall, RemoteCallNoArgs } from './common';

export type LoginRequest = {
    username: string;
    password: string;
};
export type LoginResponse = {
    accessToken: string;
    tokenHeader: string;
};

export type StudentRegistrationRequest = {
    username: string;
    password: string;
    email: string;
    firstName: string;
    lastName?: string;
    address: string;
    phoneNumber: string;
    dateOfBirth: Date;
    fathersName: string;
    mothersName: string;
};

export type StudentRegistrationResponse = {
    id: string;
    username: string;
    email: string;
    firstName: string;
    lastName?: string;
    address: string;
    phoneNumber: string;
    dateOfBirth: Date;
    fathersName: string;
    mothersName: string;
    status: string;
};

export type InstructorRegistrationRequest = {
    username: string;
    password: string;
    email: string;
    firstName: string;
    lastName?: string;
    phoneNumber: string;
    department: string;
};

export type InstructorRegistrationResponse = {
    id: string;
    username: string;
    password: string;
    email: string;
    firstName: string;
    lastName?: string;
    phoneNumber: string;
    department: string;
    status: string;
};

export interface IApi {
    auth: IAuthApi;
    registration: IRegistrationApi;
}

export interface IAuthApi {
    login: RemoteCall<LoginRequest, LoginResponse>;
    me: RemoteCallNoArgs<AuthUser>;
}

export interface IRegistrationApi {
    createStudentRegistration: RemoteCall<StudentRegistrationRequest, StudentRegistrationResponse>;

    createInstructorRegistration: RemoteCall<InstructorRegistrationRequest, InstructorRegistrationResponse>;
}
