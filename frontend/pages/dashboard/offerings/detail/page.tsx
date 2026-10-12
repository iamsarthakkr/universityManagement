import { Navigate, useParams } from 'react-router';

import { OfferingDetail } from '@/components/offerings/OfferingDetail';
import { idParam } from '@/hooks/useFilterParams';

export default function OfferingDetailPage() {
    const offeringId = idParam(useParams().offeringId ?? '');

    if (!offeringId) {
        return <Navigate to="/dashboard" replace />;
    }

    return <OfferingDetail key={offeringId} offeringId={offeringId} />;
}
