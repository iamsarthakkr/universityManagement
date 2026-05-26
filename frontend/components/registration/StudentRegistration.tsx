import { AuthShell } from '../layout/AuthShell';
import { StudentRegistrationForm } from './StudentRegistrationForm';

export const StudentRegistration = () => {
    return (
        <AuthShell>
            <StudentRegistrationForm />
        </AuthShell>
    );
};
