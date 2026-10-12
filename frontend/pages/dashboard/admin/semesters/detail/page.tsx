import { Navigate, useParams } from 'react-router';

import { SemesterDetail } from '@/components/semesters/SemesterDetail';
import { idParam } from '@/hooks/useFilterParams';

export default function SemesterDetailPage() {
    const semesterId = idParam(useParams().semesterId ?? '');

    if (!semesterId) {
        return <Navigate to="/dashboard/admin/semesters" replace />;
    }

    return <SemesterDetail key={semesterId} semesterId={semesterId} />;
}
