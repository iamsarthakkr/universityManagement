export type RemoteRes<T> = {
    message: string;
    body?: T;
    errors?: Record<string, string>;
    isSuccess: boolean;
    timestamp: Date;
    // HTTP status code; 0 when the request never got a response (network/CORS failure).
    status: number;
};

export type Callback = () => void;
export type Callback1<T> = (arg: T) => void;

export type RemoteCall<T, R> = (arg: T) => Promise<RemoteRes<R>>;
export type RemoteCallNoArgs<R> = () => Promise<RemoteRes<R>>;
