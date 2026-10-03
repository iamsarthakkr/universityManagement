import { ThemeToggle } from '@/components/common/ThemeToggle';
import { AppSidebar } from '@/components/dashboard/AppSidebar';
import { Breadcrumb, BreadcrumbItem, BreadcrumbList, BreadcrumbPage } from '@/components/ui/base/breadcrumb';
import { Separator } from '@/components/ui/base/separator';
import { SidebarInset, SidebarProvider, SidebarTrigger } from '@/components/ui/base/sidebar';
import { UNIV_NAME } from '@/config/common';
import { useAppStore } from '@/stores/appStore';
import { Navigate, Outlet } from 'react-router';

export default function DashboardLayout() {
    const user = useAppStore((state) => state.user);

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    return (
        <SidebarProvider>
            <AppSidebar />
            <SidebarInset>
                <header className="flex h-16 shrink-0 items-center gap-2 transition-[width,height] ease-linear group-has-data-[collapsible=icon]/sidebar-wrapper:h-12">
                    <div className="flex items-center gap-2 px-4">
                        <SidebarTrigger className="-ml-1" />
                        <Separator orientation="vertical" className="mr-2 data-vertical:h-4 data-vertical:self-auto" />
                        <Breadcrumb>
                            <BreadcrumbList>
                                <BreadcrumbItem>
                                    <BreadcrumbPage className="text-sm font-semibold uppercase tracking-[0.2em] text-brand">
                                        {UNIV_NAME}
                                    </BreadcrumbPage>
                                </BreadcrumbItem>
                            </BreadcrumbList>
                        </Breadcrumb>
                    </div>
                    <div className="ml-auto px-4">
                        <ThemeToggle />
                    </div>
                </header>
                <div className="flex flex-1 flex-col gap-4 p-4 pt-0">
                    <Outlet />
                </div>
            </SidebarInset>
        </SidebarProvider>
    );
}
