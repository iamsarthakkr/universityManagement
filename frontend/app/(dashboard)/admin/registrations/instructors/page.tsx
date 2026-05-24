import { PageHeader } from '@/components/common/PageHeader';
import { pendingInstructorRegistrations } from '@/lib/mock-data';

export default function InstructorRegistrationsPage() {
    return (
        <>
            <PageHeader
                title="Pending instructor registrations"
                description="Dummy table for admin review of instructor registration requests."
            />
            <div className="overflow-hidden rounded-3xl border border-border bg-white shadow-soft">
                <table className="w-full text-left text-sm">
                    <thead className="bg-surface-muted text-text-muted">
                        <tr>
                            <th className="p-4">Name</th>
                            <th className="p-4">Username</th>
                            <th className="p-4">Email</th>
                            <th className="p-4">Submitted</th>
                            <th className="p-4">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        {pendingInstructorRegistrations.map((item) => (
                            <tr key={item.id} className="border-t border-border">
                                <td className="p-4 font-semibold">{item.name}</td>
                                <td className="p-4">{item.username}</td>
                                <td className="p-4">{item.email}</td>
                                <td className="p-4">{item.submittedAt}</td>
                                <td className="p-4 text-brand font-semibold">Review</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </>
    );
}
