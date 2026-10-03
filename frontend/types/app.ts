import { AuthUser } from './auth';
import { Department } from './department';

export enum AppState {
    LOADING = 'LOADING',
    FAILED = 'FAILED',
    READY = 'READY',
}

export type StaticData = {
    departments: Department[];
};

export type LoginResult = {
    success: boolean;
    message: string;
};

export type AppStoreData = {
    appState: AppState;
    error: string | null;
    user: AuthUser | null;
    staticData: StaticData;
};

export type AppStoreActions = {
    init: () => Promise<void>;
    login: (username: string, password: string) => Promise<LoginResult>;
    logout: () => void;
    expireSession: () => void;
};
