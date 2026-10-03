import { RegistrationKind, RegistrationStatus } from '@/types/registration';

export const queryKeys = {
    registrations: {
        byKind: (kind: RegistrationKind) => ['registrations', kind] as const,
        list: (kind: RegistrationKind, status: RegistrationStatus) => ['registrations', kind, status] as const,
    },
    courses: {
        all: ['courses'] as const,
        catalogue: ['courses', 'catalogue'] as const,
    },
};
