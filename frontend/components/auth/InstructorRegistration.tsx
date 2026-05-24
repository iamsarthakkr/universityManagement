import { AuthShell } from '../layout/AuthShell';
import { InstructorRegistrationForm } from './InstructorRegistrationForm';

export const InstructorRegistration = () => {
    return (
        <AuthShell>
            <InstructorRegistrationForm />
        </AuthShell>
    );
};
