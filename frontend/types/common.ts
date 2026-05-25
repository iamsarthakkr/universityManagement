export type RemoteRes<T> = {
    message: string;
    body?: T;
    errors?: Record<string, string>;
    isSuccess: boolean;
    timestamp: Date;
};

export type Callback = () => void;
export type Callback1<T> = (arg: T) => void;

export type RemoteCall<T, R> = (arg: T) => Promise<RemoteRes<R>>;
export type RemoteCallNoArgs<R> = () => Promise<RemoteRes<R>>;
