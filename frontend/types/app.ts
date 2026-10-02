import { Department } from './department';

export enum AppState {
    LOADING = 'LOADING',
    FAILED = 'FAILED',
    READY = 'READY',
}

export type StaticData = {
    departments: Department[];
};
