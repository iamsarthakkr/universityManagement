import { create } from 'zustand';

import { createApi } from '@/lib/api/api';
import { IApi } from '@/types/IApi';

type ApiStore = {
    api: IApi;
};

export const useApiStore = create<ApiStore>()(() => ({
    api: createApi(),
}));

export const useApi = () => useApiStore((state) => state.api);
